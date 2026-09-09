package org.store.store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.ProductRequest;
import org.store.store.model.Product;
import org.store.store.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderService orderService;

    public ProductService(ProductRepository productRepository, OrderService orderService) {
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsContains(String search, Pageable pageable) {
        return productRepository.findProductByNameContainsIgnoreCase(search, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return productRepository.findProductByAvailableTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public Product addProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setAvailable(request.isAvailable());
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());

        return productRepository.save(product);
    }

    @Transactional
    public Product editProduct(ProductRequest updated, int id) {
        Product product = getProductById(id);

        product.setName(updated.getName());
        product.setImageUrl(updated.getImageUrl());
        product.setStock(updated.getStock());
        product.setAvailable(updated.isAvailable());
        product.setPrice(updated.getPrice());
        product.setDescription(updated.getDescription());

        Product saved = productRepository.save(product);

        orderService.syncPendingCartsForProduct(saved, null);

        return saved;
    }

    @Transactional
    public void deleteProduct(int id) {
        if (!productRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        productRepository.deleteById(id);
    }
}