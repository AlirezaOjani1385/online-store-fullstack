package org.store.store.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.store.store.model.Product;
import org.store.store.model.Role;
import org.store.store.model.User;
import org.store.store.repository.ProductRepository;
import org.store.store.repository.UserRepository;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (userRepository.findByNumber("09111111111").isEmpty()) {
            User admin = new User("2000-01-01", "abcd@gmail.com", "Ali", "Hassani",
                    "09111111111", passwordEncoder.encode("1234"), Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        if (userRepository.findByNumber("09222222222").isEmpty()) {
            User user = new User("2000-01-01", "efgh@gmail.com", "Fatemeh", "Ahmadi",
                    "09222222222", passwordEncoder.encode("5678"), Role.USER);
            user.setEnabled(true);
            userRepository.save(user);
        }

        if (productRepository.count() == 0) {
            Product p1 = new Product(true, "کیف سفید کوچک به ابعاد 12 در 20", "http://localhost:8080/images/1.webp", "کیف", 1_500_000, 5);
            Product p2 = new Product(true, "کلاه کپ طرح طوطی صورتی و ابی و قرمز", "http://localhost:8080/images/2.jpg",  "کلاه", 500_000, 3);
            Product p3 = new Product(true, "دست کش مشکی زنانه", "http://localhost:8080/images/3.webp", "دست کش", 1_000_000, 10);
            productRepository.saveAll(List.of(p1, p2, p3));
        }
    }
}