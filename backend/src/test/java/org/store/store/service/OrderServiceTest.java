package org.store.store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.CheckoutRequest;
import org.store.store.dto.CreateOrderRequest;
import org.store.store.dto.OrderItemRequest;
import org.store.store.model.*;
import org.store.store.repository.CheckoutRepository;
import org.store.store.repository.OrderRepository;
import org.store.store.repository.ProductRepository;
import org.store.store.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CheckoutRepository checkoutRepository;

    @InjectMocks
    private OrderService orderService;

    private User sampleUser;
    private Order sampleOrder;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setNumber("09121111111");
        sampleUser.setRole(Role.USER);

        sampleProduct = new Product();
        sampleProduct.setId(100);
        sampleProduct.setName("محصول تست");
        sampleProduct.setPrice(50.0);
        sampleProduct.setStock(10);
        sampleProduct.setAvailable(true);

        OrderItem sampleItem = new OrderItem();
        sampleItem.setId(10);
        sampleItem.setProduct(sampleProduct);
        sampleItem.setQuantity(2);

        sampleOrder = new Order();
        sampleOrder.setId(1);
        sampleOrder.setUser(sampleUser);
        sampleOrder.setStatus(OrderStatus.PENDING);
        sampleOrder.setTotalPrice(100.0);
        sampleOrder.setItems(new ArrayList<>(List.of(sampleItem)));
    }

    @Test
    void testGetAllOrders() {
        Page<Order> page = new PageImpl<>(List.of(sampleOrder));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Order> result = orderService.getAllOrders(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void testGetOrderByIdSuccess() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));

        Order result = orderService.getOrderById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.getOrderById(99));
    }

    @Test
    void testGetOrderByIdWithOwnershipSuccess() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));

        Order result = orderService.getOrderById(1, "09121111111");

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testGetOrderByIdWithOwnershipForbidden() {
        User otherUser = new User();
        otherUser.setNumber("09999999999");
        otherUser.setRole(Role.USER);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09999999999")).thenReturn(Optional.of(otherUser));

        assertThrows(ResponseStatusException.class, () -> orderService.getOrderById(1, "09999999999"));
    }

    @Test
    void testGetOrdersOfUserByNumber() {
        Page<Order> page = new PageImpl<>(List.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(orderRepository.getOrdersByUserIs(eq(sampleUser), any(Pageable.class))).thenReturn(page);

        Page<Order> result = orderService.getOrdersOfUserByNumber("09121111111", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetOrdersOfUserById() {
        Page<Order> page = new PageImpl<>(List.of(sampleOrder));
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(orderRepository.getOrdersByUserIs(eq(sampleUser), any(Pageable.class))).thenReturn(page);

        Page<Order> result = orderService.getOrdersOfUserById(1, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testAddOrderSuccess() {
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setQuantity(2);
        itemRequest.setProductId(100);
        request.setItems(List.of(itemRequest));
        Order result = orderService.addOrder(request, "09121111111");

        assertNotNull(result);
        assertEquals(100.0, result.getTotalPrice());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testAddItemSuccess() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(100);
        request.setQuantity(2);
        Order result = orderService.addItem(request, 1, "09121111111");

        assertNotNull(result);
        verify(orderRepository).save(sampleOrder);
    }

    @Test
    void testEditItemOfOrderSuccess() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(100);
        request.setQuantity(5);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order result = orderService.editItemOfOrder(request, 1, 10, "09121111111");

        assertNotNull(result);
        assertEquals(250.0, result.getTotalPrice());
    }

    @Test
    void testEditItemOfOrderItemNotFound() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(100);
        request.setQuantity(5);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(100)).thenReturn(Optional.of(sampleProduct));

        assertThrows(ResponseStatusException.class, () -> orderService.editItemOfOrder(request, 1, 999, "09121111111"));
    }

    @Test
    void testDeleteOrderPendingSuccess() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        doNothing().when(orderRepository).deleteById(1);

        assertDoesNotThrow(() -> orderService.deleteOrder(1, "09121111111"));
        verify(orderRepository).deleteById(1);
        verify(productRepository, never()).save(any());
    }

    @Test
    void testDeleteOrderProcessingRestoresStock() {
        sampleOrder.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        doNothing().when(orderRepository).deleteById(1);

        assertDoesNotThrow(() -> orderService.deleteOrder(1, "09121111111"));

        assertEquals(12, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
        verify(orderRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteOrderItemSuccess() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order result = orderService.deleteOrderItem(1, 10, "09121111111");

        assertNotNull(result);
        assertEquals(0.0, sampleOrder.getTotalPrice());
        assertTrue(sampleOrder.getItems().isEmpty());
    }

    @Test
    void testAddCheckoutSuccessDeductsStock() {
        CheckoutRequest request = new CheckoutRequest();
        request.setAddress("تهران، خیابان آزادی");
        request.setPostalCode("1234567890");

        Checkout expectedCheckout = new Checkout();
        expectedCheckout.setId(5);
        expectedCheckout.setAddress(request.getAddress());
        expectedCheckout.setPostalCode(request.getPostalCode());
        expectedCheckout.setTotalPrice(200100.0);
        expectedCheckout.setOrder(sampleOrder);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(expectedCheckout);

        Checkout actual = orderService.addCheckout(request, "09121111111", 1);

        assertNotNull(actual);
        assertEquals(8, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
        verify(checkoutRepository).save(any(Checkout.class));
    }

    @Test
    void testAddCheckoutEmptyCartThrowsException() {
        sampleOrder.getItems().clear();

        CheckoutRequest request = new CheckoutRequest();
        request.setAddress("تهران");
        request.setPostalCode("1234567890");

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.addCheckout(request, "09121111111", 1));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(checkoutRepository, never()).save(any());
    }

    @Test
    void testAddCheckoutAccessDeniedThrowsException() {
        User otherUser = new User();
        otherUser.setNumber("09999999999");
        otherUser.setRole(Role.USER);

        CheckoutRequest request = new CheckoutRequest();

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09999999999")).thenReturn(Optional.of(otherUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.addCheckout(request, "09999999999", 1));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(checkoutRepository, never()).save(any());
    }

    @Test
    public void cancelOrderSuccessWhenStatusIsProcessingRestoresStock() {
        sampleOrder.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order updatedOrder = orderService.cancelOrder(1, "09121111111");

        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
        assertEquals(12, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    public void cancelOrderThrowsExceptionWhenStatusIsNotCancellable() {
        sampleOrder.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09121111111")).thenReturn(Optional.of(sampleUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(1, "09121111111"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    public void cancelOrderThrowsExceptionWhenUserIsNotOwner() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(sampleOrder));
        when(userRepository.findByNumber("09222222222")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(1, "09222222222"));

        verify(orderRepository, never()).save(any());
    }
}