package org.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.store.store.model.Checkout;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, Integer> {
}
