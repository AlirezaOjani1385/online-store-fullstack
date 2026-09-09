package org.store.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.CheckoutRequest;
import org.store.store.dto.CreateOrderRequest;
import org.store.store.dto.OrderItemRequest;
import org.store.store.model.Checkout;
import org.store.store.model.Order;
import org.store.store.model.OrderStatus;
import org.store.store.security.JwtUtils;
import org.store.store.security.RateLimitingService;
import org.store.store.service.OrderService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsernamePasswordAuthenticationToken auth;
    private Order sampleOrder;
    private Page<Order> sampleOrderPage;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService.reset();

        auth = new UsernamePasswordAuthenticationToken("09121111111", null);

        sampleOrder = new Order();
        sampleOrder.setId(1);

        sampleOrderPage = new PageImpl<>(List.of(sampleOrder));
    }

    @Test
    void testGetAllOrders() throws Exception {
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(sampleOrderPage);

        mockMvc.perform(get("/store/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderById(eq(1), anyString())).thenReturn(sampleOrder);

        mockMvc.perform(get("/store/orders/1").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetOrdersOfUserByNumber() throws Exception {
        when(orderService.getOrdersOfUserByNumber(eq("09121111111"), any(Pageable.class))).thenReturn(sampleOrderPage);

        mockMvc.perform(get("/store/orders/user").param("number", "09121111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void testGetOrdersOfMe() throws Exception {
        when(orderService.getOrdersOfUserByNumber(eq("09121111111"), any(Pageable.class))).thenReturn(sampleOrderPage);

        mockMvc.perform(get("/store/orders/user/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void testGetOrdersOfUserById() throws Exception {
        when(orderService.getOrdersOfUserById(eq(1), any(Pageable.class))).thenReturn(sampleOrderPage);

        mockMvc.perform(get("/store/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void testAddOrder() throws Exception {
        CreateOrderRequest inputRequest = new CreateOrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(100);
        item.setQuantity(2);
        inputRequest.setItems(List.of(item));

        when(orderService.addOrder(any(CreateOrderRequest.class), anyString())).thenReturn(sampleOrder);

        mockMvc.perform(post("/store/orders/user/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testAddItem() throws Exception {
        OrderItemRequest inputRequest = new OrderItemRequest();
        inputRequest.setProductId(100);
        inputRequest.setQuantity(1);

        when(orderService.addItem(any(OrderItemRequest.class), eq(1), anyString())).thenReturn(sampleOrder);

        mockMvc.perform(post("/store/orders/1")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testEditOrder() throws Exception {
        OrderItemRequest inputRequest = new OrderItemRequest();
        inputRequest.setProductId(100);
        inputRequest.setQuantity(2);

        when(orderService.editItemOfOrder(any(OrderItemRequest.class), eq(1), eq(10), anyString())).thenReturn(sampleOrder);

        mockMvc.perform(put("/store/orders/1/10")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteItem() throws Exception {
        when(orderService.deleteOrderItem(eq(1), eq(10), anyString())).thenReturn(sampleOrder);

        mockMvc.perform(delete("/store/orders/1/10").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(eq(1), anyString());

        mockMvc.perform(delete("/store/orders/1").principal(auth))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAddCheckout() throws Exception {
        CheckoutRequest inputRequest = new CheckoutRequest();
        inputRequest.setAddress("تهران، خیابان آزادی");
        inputRequest.setPostalCode("1234567890");

        Checkout sampleCheckout = new Checkout();
        sampleCheckout.setId(10);
        sampleCheckout.setAddress("تهران، خیابان آزادی");
        sampleCheckout.setPostalCode("1234567890");
        sampleCheckout.setTotalPrice(200100.0);

        when(orderService.addCheckout(any(CheckoutRequest.class), eq("09121111111"), eq(1)))
                .thenReturn(sampleCheckout);

        mockMvc.perform(post("/store/orders/user/me/checkout/1")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.address").value("تهران، خیابان آزادی"))
                .andExpect(jsonPath("$.postalCode").value("1234567890"))
                .andExpect(jsonPath("$.totalPrice").value(200100.0));
    }

    @Test
    public void cancelOrderShouldReturn200OK() throws Exception {
        Order order = new Order();
        order.setId(1);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(anyInt(), anyString())).thenReturn(order);

        mockMvc.perform(put("/store/orders/cancel/1")
                        .principal(new UsernamePasswordAuthenticationToken("09111111111", null))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    public void cancelOrderShouldReturn400BadRequestWhenServiceThrowsException() throws Exception {
        when(orderService.cancelOrder(anyInt(), anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel"));

        mockMvc.perform(put("/store/orders/cancel/1")
                        .principal(new UsernamePasswordAuthenticationToken("09111111111", null))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}