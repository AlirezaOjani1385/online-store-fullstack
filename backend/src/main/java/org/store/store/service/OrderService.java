package org.store.store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.CheckoutRequest;
import org.store.store.dto.CreateOrderRequest;
import org.store.store.dto.OrderItemRequest;
import org.store.store.model.*;
import org.store.store.repository.*;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Service
public class OrderService {

    private static final double SHIPPING_FEE = 200_000.0;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CheckoutRepository checkoutRepository;

    public OrderService(CheckoutRepository checkoutRepository, OrderRepository orderRepository, UserRepository userRepository,
                        ProductRepository productRepository) {
        this.checkoutRepository = checkoutRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private void checkOwnership(Order order, String number) {
        if (number == null) return;

        User user = userRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isOwner = order.getUser() != null && order.getUser().getNumber() != null && order.getUser().getNumber().equals(number);
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this order");
    }

    private Product resolveProduct(Integer productId) {
        if (productId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id is required");
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void checkStockAvailability(Product product, int requestedQuantity) {
        if (!product.getAvailable() || product.getStock() < requestedQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not enough stock for product: " + product.getName() + ". Available stock: " + product.getStock());
        }
    }

    private void recalculateTotalPrice(Order order) {
        double total = 0.0;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null && item.getQuantity() > 0) {
                    total += item.getProduct().getPrice() * item.getQuantity();
                }
            }
        }
        order.setTotalPrice(total);
    }

    private void restoreStockForOrder(Order order) {
        if (order.getItems() == null) return;

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
                syncPendingCartsForProduct(product, order.getId());
            }
        }
    }

    public void syncPendingCartsForProduct(Product product, Integer currentOrderId) {
        List<Order> orders = orderRepository.findPendingOrdersContainingProduct(
                product.getId(), OrderStatus.PENDING);

        for (Order order : orders) {
            if (order.getId() != null && order.getId().equals(currentOrderId))
                continue;

            boolean changed = false;
            Iterator<OrderItem> it = order.getItems().iterator();

            while (it.hasNext()) {
                OrderItem item = it.next();
                if (item.getProduct() != null && item.getProduct().getId().equals(product.getId())) {
                    if (!product.getAvailable() || product.getStock() <= 0) {
                        it.remove();
                        changed = true;
                    } else if (item.getQuantity() > product.getStock()) {
                        item.setQuantity(product.getStock());
                        changed = true;
                    }
                }
            }

            if (changed) {
                if (order.getItems().isEmpty()) {
                    orderRepository.delete(order);
                } else {
                    recalculateTotalPrice(order);
                    orderRepository.save(order);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(int id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public Order getOrderById(int id, String number) {
        Order order = getOrderById(id);
        checkOwnership(order, number);
        return order;
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersOfUserByNumber(String number, Pageable pageable) {
        User user = userRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return orderRepository.getOrdersByUserIs(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersOfUserById(int id, Pageable pageable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return orderRepository.getOrdersByUserIs(user, pageable);
    }

    @Transactional
    public Order addOrder(CreateOrderRequest request, String number) {
        User user = null;
        if (number != null) {
            user = userRepository.findByNumber(number)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = resolveProduct(itemRequest.getProductId());
            checkStockAvailability(product, itemRequest.getQuantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setOrder(order);
            order.getItems().add(item);
        }

        recalculateTotalPrice(order);

        return orderRepository.save(order);
    }

    @Transactional
    public Checkout addCheckout(CheckoutRequest request, String number, int orderId) {
        Order order = getOrderById(orderId);
        checkOwnership(order, number);

        if (order.getItems().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");

        if (order.getStatus() != OrderStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not in PENDING status");

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            checkStockAvailability(product, item.getQuantity());

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            syncPendingCartsForProduct(product, order.getId());
        }

        Checkout checkout = new Checkout();
        checkout.setAddress(request.getAddress());
        checkout.setPostalCode(request.getPostalCode());
        checkout.setTotalPrice(order.getTotalPrice() + SHIPPING_FEE);
        checkout.setOrder(order);

        order.setCheckout(checkout);
        order.setStatus(OrderStatus.PROCESSING);

        return checkoutRepository.save(checkout);
    }

    @Transactional
    public Order addItem(OrderItemRequest request, int orderId, String number) {
        Order order = getOrderById(orderId);
        checkOwnership(order, number);

        if (order.getStatus() != OrderStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not pending");

        Product product = resolveProduct(request.getProductId());
        checkStockAvailability(product, request.getQuantity());

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setOrder(order);
        order.addItem(item);
        recalculateTotalPrice(order);
        return orderRepository.save(order);
    }

    @Transactional
    public Order editItemOfOrder(OrderItemRequest request, int orderId, int itemId, String number) {
        Order order = getOrderById(orderId);
        checkOwnership(order, number);

        if (order.getStatus() != OrderStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not pending");

        Product product = resolveProduct(request.getProductId());
        checkStockAvailability(product, request.getQuantity());

        boolean found = false;
        for (OrderItem item : order.getItems()) {
            if (item.getId() != null && item.getId() == itemId) {
                item.setProduct(product);
                item.setQuantity(request.getQuantity());
                found = true;
                break;
            }
        }

        if (!found)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in this order");

        recalculateTotalPrice(order);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(int id, String number) {
        Order order = getOrderById(id);
        checkOwnership(order, number);

        if (order.getStatus() != OrderStatus.PROCESSING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending or processing orders can be cancelled");

        restoreStockForOrder(order);

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(int id, String number) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        checkOwnership(order, number);

        if (order.getStatus() == OrderStatus.PROCESSING)
            restoreStockForOrder(order);

        orderRepository.deleteById(id);
    }

    @Transactional
    public Order deleteOrderItem(int orderId, int itemId, String number) {
        Order order = getOrderById(orderId);
        checkOwnership(order, number);

        if (order.getStatus() != OrderStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not pending");

        boolean removed = order.getItems().removeIf(item -> item.getId() != null && item.getId() == itemId);

        if (!removed)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in this order");

        recalculateTotalPrice(order);
        return orderRepository.save(order);
    }
}