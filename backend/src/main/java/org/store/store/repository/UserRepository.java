package org.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.store.store.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByNumber(String number);

    boolean existsByNumber(String number);

    void deleteByNumber(String number);

    void deleteByEnabledFalseAndVerificationCodeExpiryBefore(LocalDateTime now);
}
