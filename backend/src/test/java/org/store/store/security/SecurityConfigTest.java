package org.store.store.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.store.store.repository.UserRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        rateLimitingService.reset();
    }

    @Test
    void publicEndpointsShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/store/welcome"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/store/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointsWithUserRoleShouldReturn403Forbidden() throws Exception {
        mockMvc.perform(get("/store/orders")
                        .with(user("09123456789").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointsWithAdminRoleShouldNotBeForbidden() throws Exception {
        mockMvc.perform(get("/store/orders")
                        .with(user("09121111111").roles("ADMIN")))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Expected access to be allowed, but got status: " + status);
                    }
                });
    }

    @Test
    void corsPreflightShouldBeAllowed() throws Exception {
        mockMvc.perform(options("/store/products")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }
}