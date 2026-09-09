package org.store.store.security;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.store.store.repository.UserRepository;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(@NotNull String number) throws UsernameNotFoundException {
        org.store.store.model.User user = repository.findByNumber(number)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with this number: " + number));

        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getNumber(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(authority)
        );
    }
}