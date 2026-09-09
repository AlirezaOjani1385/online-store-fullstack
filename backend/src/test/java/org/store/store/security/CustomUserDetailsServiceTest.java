package org.store.store.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.store.store.model.Role;
import org.store.store.model.User;
import org.store.store.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setNumber("09123456789");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole(Role.ADMIN);
    }

    @Test
    void loadUserByUsernameWhenUserExistsShouldReturnUserDetailsWithRole() {
        when(userRepository.findByNumber("09123456789")).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("09123456789");

        assertNotNull(userDetails);
        assertEquals("09123456789", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN")));

        verify(userRepository, times(1)).findByNumber("09123456789");
    }

    @Test
    void loadUserByUsernameWhenUserNotFoundShouldThrowUsernameNotFoundException() {
        when(userRepository.findByNumber("09999999999")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("09999999999")
        );

        verify(userRepository, times(1)).findByNumber("09999999999");
    }
}