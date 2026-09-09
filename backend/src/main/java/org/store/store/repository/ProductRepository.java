package org.store.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.store.store.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findProductByNameContainsIgnoreCase(String name, Pageable pageable);

    Page<Product> findProductByAvailableTrue(Pageable pageable);
}
