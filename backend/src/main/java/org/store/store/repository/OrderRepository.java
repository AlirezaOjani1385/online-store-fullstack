package org.store.store.repository;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.store.store.model.Order;
import org.store.store.model.OrderStatus;
import org.store.store.model.User;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query(value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.checkout WHERE o.user = :user",
            countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user = :user")
    Page<Order> getOrdersByUserIs(@Param("user") User user, Pageable pageable);

    @NullMarked
    @Query(value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.checkout",
            countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAll(Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE o.status = :status AND i.product.id = :productId")
    List<Order> findPendingOrdersContainingProduct(@Param("productId") Integer productId, @Param("status") OrderStatus status);
}
