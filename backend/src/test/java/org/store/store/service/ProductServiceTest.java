package org.store.store.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.ProductRequest;
import org.store.store.model.Product;
import org.store.store.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Mock
    private OrderService orderService;

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
    void testGetProductByContainsName() {
        List<Product> products = List.of(
                createSampleProduct(1, "کلاه قرمز", 12.5, 10, true, "\\images\\1.jpg", "***"),
                createSampleProduct(2, "کلاه ابی", 13.5, 5, true, "\\images\\2.jpg", "****")
        );
        Page<Product> expectedPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findProductByNameContainsIgnoreCase(eq("کلاه"), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Product> actual = service.getProductsContains("کلاه", pageable);

        assertNotNull(actual);
        assertEquals(2, actual.getContent().size());
        assertEquals(products, actual.getContent());

        verify(repository, times(1)).findProductByNameContainsIgnoreCase(eq("کلاه"), any(Pageable.class));
    }

    @Test
    void testGetProductByContainsNameIsEmpty() {
        Page<Product> expectedPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findProductByNameContainsIgnoreCase(eq("کلاه"), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Product> actual = service.getProductsContains("کلاه", pageable);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());

        verify(repository, times(1)).findProductByNameContainsIgnoreCase(eq("کلاه"), any(Pageable.class));
    }

    @Test
    void testGetAvailableProducts() {
        List<Product> products = List.of(
                createSampleProduct(1, "کلاه قرمز", 12.5, 10, true, "\\images\\1.jpg", "***"),
                createSampleProduct(2, "کلاه ابی", 13.5, 5, true, "\\images\\2.jpg", "****")
        );
        Page<Product> expectedPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findProductByAvailableTrue(any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Product> actual = service.getAvailableProducts(pageable);

        assertNotNull(actual);
        assertEquals(2, actual.getContent().size());
        assertEquals(products, actual.getContent());

        verify(repository, times(1)).findProductByAvailableTrue(any(Pageable.class));
    }

    @Test
    void testGetAvailableProductsIsEmpty() {
        Page<Product> expectedPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findProductByAvailableTrue(any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Product> actual = service.getAvailableProducts(pageable);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());

        verify(repository, times(1)).findProductByAvailableTrue(any(Pageable.class));
    }

    @Test
    void testGetProductByIdFound() {
        Product expected = createSampleProduct(1, "کلاه", 12.5, 10, true, "\\images\\1.jpg", "***");

        when(repository.findById(1)).thenReturn(Optional.of(expected));

        Product actual = service.getProductById(1);

        assertEquals(expected.getImageUrl(), actual.getImageUrl());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getStock(), actual.getStock());
        assertEquals(expected.getAvailable(), actual.getAvailable());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDescription(), actual.getDescription());

        verify(repository, times(1)).findById(1);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getProductById(99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repository, times(1)).findById(99);
    }

    @Test
    void testAddProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("کلاه");
        request.setImageUrl("\\images\\1.jpg");
        request.setPrice(12.5);
        request.setStock(15);
        request.setAvailable(true);
        request.setDescription("***");

        Product expectedSavedProduct = createSampleProduct(1, "کلاه", 12.5, 15, true, "\\images\\1.jpg", "***");

        when(repository.save(any(Product.class))).thenReturn(expectedSavedProduct);

        Product actual = service.addProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository, times(1)).save(captor.capture());

        Product captured = captor.getValue();
        assertEquals("کلاه", captured.getName());
        assertEquals(12.5, captured.getPrice());
        assertEquals(15, captured.getStock());
        assertTrue(captured.getAvailable());
        assertEquals("\\images\\1.jpg", captured.getImageUrl());
        assertEquals("***", captured.getDescription());

        assertEquals(expectedSavedProduct.getId(), actual.getId());
    }

    @Test
    void testEditProductFound() {
        ProductRequest request = new ProductRequest();
        request.setName("کلاه جدید");
        request.setImageUrl("\\images\\1.jpg");
        request.setPrice(15.0);
        request.setStock(20);
        request.setAvailable(true);
        request.setDescription("توضیحات جدید");

        Product existingInDb = createSampleProduct(1, "شال قدیمی", 14.5, 5, false,
                "\\images\\old.jpg", "قدیمی");
        Product updatedProduct = createSampleProduct(1, "کلاه جدید", 15.0, 20, true,
                "\\images\\1.jpg", "توضیحات جدید");

        when(repository.findById(1)).thenReturn(Optional.of(existingInDb));
        when(repository.save(any(Product.class))).thenReturn(updatedProduct);

        Product actual = service.editProduct(request, 1);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository, times(1)).save(captor.capture());

        Product captured = captor.getValue();
        assertEquals("کلاه جدید", captured.getName());
        assertEquals(15.0, captured.getPrice());
        assertEquals(20, captured.getStock());
        assertTrue(captured.getAvailable());

        assertEquals(updatedProduct.getId(), actual.getId());
        verify(repository, times(1)).findById(1);
        verify(orderService, times(1)).syncPendingCartsForProduct(any(Product.class), eq(null));
    }

    @Test
    void testEditProductNotFound() {
        ProductRequest request = new ProductRequest();
        request.setName("کلاه");
        request.setImageUrl("\\images\\1.jpg");
        request.setPrice(12.5);
        request.setStock(10);
        request.setAvailable(true);
        request.setDescription("***");

        when(repository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.editProduct(request, 99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(repository, never()).save(any(Product.class));
        verify(repository, times(1)).findById(99);
    }

    @Test
    void testDeleteProductFound() {
        when(repository.existsById(1)).thenReturn(true);
        doNothing().when(repository).deleteById(1);

        service.deleteProduct(1);

        verify(repository, times(1)).deleteById(1);
        verify(repository, times(1)).existsById(1);
    }

    @Test
    void testDeleteProductNotFound() {
        when(repository.existsById(100)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteProduct(100));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(repository, times(1)).existsById(100);
        verify(repository, never()).deleteById(100);
    }
}