package org.store.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.store.store.dto.ProductRequest;
import org.store.store.model.Product;
import org.store.store.security.JwtUtils;
import org.store.store.security.RateLimitingService;
import org.store.store.security.SecurityConfig;
import org.store.store.service.ProductService;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService.reset();
    }

    private Product createSampleProduct(int id, String name, double price, int stock, boolean available, String img, String desc) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setAvailable(available);
        p.setImageUrl(img);
        p.setDescription(desc);
        return p;
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(createSampleProduct(1, "کلاه قرمز", 12.5, 10, true, "\\images\\1.jpg", "***"));
        Page<Product> page = new PageImpl<>(products);

        when(service.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/store/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].price").value(12.5))
                .andExpect(jsonPath("$.content[0].stock").value(10))
                .andExpect(jsonPath("$.content[0].name").value("کلاه قرمز"))
                .andExpect(jsonPath("$.content[0].available").value(true))
                .andExpect(jsonPath("$.content[0].imageUrl").value("\\images\\1.jpg"))
                .andExpect(jsonPath("$.content[0].description").value("***"));

        verify(service, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetProductsSearch() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(createSampleProduct(1, "کلاه قرمز", 12.5, 10, true, "\\images\\1.jpg", "***"));
        products.add(createSampleProduct(2, "کلاه ابی", 13.5, 0, false, "\\images\\2.jpg", "****"));
        Page<Product> page = new PageImpl<>(products);

        when(service.getProductsContains(eq("کلاه"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/store/products/search").param("name", "کلاه"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].price").value(12.5))
                .andExpect(jsonPath("$.content[0].stock").value(10))
                .andExpect(jsonPath("$.content[0].name").value("کلاه قرمز"))
                .andExpect(jsonPath("$.content[0].available").value(true))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].price").value(13.5))
                .andExpect(jsonPath("$.content[1].stock").value(0))
                .andExpect(jsonPath("$.content[1].name").value("کلاه ابی"))
                .andExpect(jsonPath("$.content[1].available").value(false));

        verify(service, times(1)).getProductsContains(eq("کلاه"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetProductAvailable() throws Exception {
        List<Product> availableProducts = new ArrayList<>();
        availableProducts.add(createSampleProduct(1, "کلاه قرمز", 12.5, 5, true, "\\images\\1.jpg", "***"));
        Page<Product> page = new PageImpl<>(availableProducts);

        when(service.getAvailableProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/store/products/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].stock").value(5))
                .andExpect(jsonPath("$.content[0].price").value(12.5))
                .andExpect(jsonPath("$.content[0].name").value("کلاه قرمز"))
                .andExpect(jsonPath("$.content[0].available").value(true));

        verify(service, times(1)).getAvailableProducts(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetProductById() throws Exception {
        Product product = createSampleProduct(1, "کلاه قرمز", 12.5, 10, true, "\\images\\1.jpg", "***");
        when(service.getProductById(1)).thenReturn(product);

        mockMvc.perform(get("/store/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.price").value(12.5))
                .andExpect(jsonPath("$.name").value("کلاه قرمز"))
                .andExpect(jsonPath("$.available").value(true));

        verify(service, times(1)).getProductById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("کلاه سبز");
        request.setPrice(15.0);
        request.setStock(20);
        request.setAvailable(true);
        request.setImageUrl("\\images\\3.jpg");
        request.setDescription("***");

        Product savedProduct = createSampleProduct(3, "کلاه سبز", 15.0, 20, true, "\\images\\3.jpg", "***");
        when(service.addProduct(any(ProductRequest.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/store/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("کلاه سبز"))
                .andExpect(jsonPath("$.price").value(15.0))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.available").value(true));

        verify(service, times(1)).addProduct(any(ProductRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("کلاه قرمز بروزرسانی شده");
        request.setPrice(18.0);
        request.setStock(15);
        request.setAvailable(true);
        request.setImageUrl("\\images\\1.jpg");
        request.setDescription("***");

        Product updatedProduct = createSampleProduct(1, "کلاه قرمز بروزرسانی شده", 18.0, 15, true, "\\images\\1.jpg", "***");
        when(service.editProduct(any(ProductRequest.class), eq(1))).thenReturn(updatedProduct);

        mockMvc.perform(put("/store/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("کلاه قرمز بروزرسانی شده"))
                .andExpect(jsonPath("$.stock").value(15))
                .andExpect(jsonPath("$.price").value(18.0));

        verify(service, times(1)).editProduct(any(ProductRequest.class), eq(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteProduct() throws Exception {
        doNothing().when(service).deleteProduct(1);

        mockMvc.perform(delete("/store/products/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteProduct(1);
    }
}