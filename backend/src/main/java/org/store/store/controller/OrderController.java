package org.store.store.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.store.store.dto.CheckoutRequest;
import org.store.store.dto.CreateOrderRequest;
import org.store.store.dto.OrderItemRequest;
import org.store.store.model.Checkout;
import org.store.store.model.Order;
import org.store.store.service.OrderService;

@RestController
@RequestMapping("/store/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                    @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable int id, Authentication authentication) {
        Order order = orderService.getOrderById(id, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<Order>> getOrdersOfUserByNumber(@RequestParam("number") String number,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "id") String sortBy,
                                                               @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getOrdersOfUserByNumber(number, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/me")
    public ResponseEntity<Page<Order>> getOrdersOfMe(Authentication authentication,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                     @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getOrdersOfUserByNumber(authentication.getName(), pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Page<Order>> getOrdersOfUserById(@PathVariable int id, @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "id") String sortBy,
                                                           @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getOrdersOfUserById(id, pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/user/me")
    public ResponseEntity<Order> addOrder(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        Order o = orderService.addOrder(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(o);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Order> addItem(@Valid @RequestBody OrderItemRequest request, @PathVariable int id, Authentication authentication) {
        Order order = orderService.addItem(request, id, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/user/me/checkout/{orderId}")
    public ResponseEntity<Checkout> addCheckout(@Valid @RequestBody CheckoutRequest request, @PathVariable int orderId,
                                                Authentication authentication) {
        Checkout checkout = orderService.addCheckout(request, authentication.getName(), orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(checkout);
    }

    @PutMapping("/{orderId}/{itemId}")
    public ResponseEntity<Order> editOrder(@Valid @RequestBody OrderItemRequest request, @PathVariable int orderId,
                                           @PathVariable int itemId, Authentication authentication) {
        Order order = orderService.editItemOfOrder(request, orderId, itemId, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Order> cancelOrder(@PathVariable int id, Authentication authentication) {
        Order order = orderService.cancelOrder(id, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}/{itemId}")
    public ResponseEntity<Order> deleteItem(@PathVariable int orderId, @PathVariable int itemId,
                                            Authentication authentication) {
        Order order = orderService.deleteOrderItem(orderId, itemId, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int id, Authentication authentication) {
        orderService.deleteOrder(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
