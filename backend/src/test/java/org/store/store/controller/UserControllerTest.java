package org.store.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.store.store.dto.UserUpdateRequest;
import org.store.store.model.Role;
import org.store.store.model.User;
import org.store.store.security.JwtUtils;
import org.store.store.security.RateLimitingService;
import org.store.store.security.SecurityConfig;
import org.store.store.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        rateLimitingService.reset();

        sampleUser = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1, "Omani",
                "09111111111", "1234", Role.ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllUsers() throws Exception {
        Page<User> page = new PageImpl<>(List.of(sampleUser));
        when(service.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/store/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$.content[0].email").value("abcd@gmail.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("Alireza"))
                .andExpect(jsonPath("$.content[0].lastName").value("Omani"))
                .andExpect(jsonPath("$.content[0].number").value("09111111111"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(service, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserByNumber() throws Exception {
        when(service.getUserByNumber("09111111111")).thenReturn(sampleUser);

        mockMvc.perform(get("/store/users/by-number").param("number", "09111111111"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$.email").value("abcd@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Alireza"))
                .andExpect(jsonPath("$.lastName").value("Omani"))
                .andExpect(jsonPath("$.number").value("09111111111"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.id").value(1));

        verify(service, times(1)).getUserByNumber("09111111111");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserById() throws Exception {
        when(service.getUserById(1)).thenReturn(sampleUser);

        mockMvc.perform(get("/store/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$.email").value("abcd@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Alireza"))
                .andExpect(jsonPath("$.lastName").value("Omani"))
                .andExpect(jsonPath("$.number").value("09111111111"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.id").value(1));

        verify(service, times(1)).getUserById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditUserByNumber() throws Exception {
        User updated = new User("2000-01-01", "abcd@gmail.com", "Alireza", 2, "Omani",
                "09111111111", "1234", Role.ADMIN);

        when(service.editUserByNumber(any(UserUpdateRequest.class), eq("09111111111"))).thenReturn(updated);

        mockMvc.perform(put("/store/users")
                        .param("number", "09111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2));

        verify(service, times(1)).editUserByNumber(any(UserUpdateRequest.class), eq("09111111111"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditUserById() throws Exception {
        User updated = new User("2000-01-01", "abcd@gmail.com", "Alireza", 2, "Omani",
                "09111111111", "1234", Role.ADMIN);

        when(service.editUserById(any(UserUpdateRequest.class), eq(2))).thenReturn(updated);

        mockMvc.perform(put("/store/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2));

        verify(service, times(1)).editUserById(any(UserUpdateRequest.class), eq(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUserById() throws Exception {
        doNothing().when(service).deleteUserById(2);

        mockMvc.perform(delete("/store/users/2"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteUserById(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUserByNumber() throws Exception {
        doNothing().when(service).deleteUserByNumber("09111111111");

        mockMvc.perform(delete("/store/users")
                        .param("number", "09111111111"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteUserByNumber("09111111111");
    }
}