package org.store.store.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.store.store.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {

    private final UserRepository userRepository;

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void removeExpiredUnverifiedUsers() {
        userRepository.deleteByEnabledFalseAndVerificationCodeExpiryBefore(LocalDateTime.now());
    }
}