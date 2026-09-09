package org.store.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.store.store.dto.LoginRequest;
import org.store.store.dto.VerifyRegistrationRequest;
import org.store.store.model.Role;

import org.store.store.security.CustomUserDetailsService;
import org.store.store.security.JwtUtils;
import org.store.store.security.RateLimitingService;
import org.store.store.service.UserService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService.reset();
    }

    @Test
    void registerShouldReturnSuccessMessage() throws Exception {
        org.store.store.model.User user = new org.store.store.model.User("2000-01-01", "abcd@gmail.com",
                "Alireza", 1, "Omani", "09111111111", "1234", Role.USER);

        when(service.addUser(any())).thenReturn(user);

        mockMvc.perform(post("/store/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Verification code sent to your phone number"));
    }

    @Test
    void verifyRegistrationShouldReturnJwtToken() throws Exception {
        VerifyRegistrationRequest verifyRequest = new VerifyRegistrationRequest("09111111111", "123456");
        UserDetails userDetails = User.withUsername("09111111111").password("1234").authorities("USER").build();

        doNothing().when(service).verifyRegistration(any());
        when(userDetailsService.loadUserByUsername(verifyRequest.getNumber())).thenReturn(userDetails);
        when(jwtUtils.generateToken(any())).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/store/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void loginShouldReturnJwtToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("09111111111", "1234");
        UserDetails userDetails = User.withUsername("09111111111").password("1234").authorities("USER").build();

        when(userDetailsService.loadUserByUsername(loginRequest.getNumber())).thenReturn(userDetails);
        when(jwtUtils.generateToken(any())).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/store/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }
}