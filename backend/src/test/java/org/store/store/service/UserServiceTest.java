package org.store.store.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.RegisterRequest;
import org.store.store.dto.ResetPasswordRequest;
import org.store.store.dto.UserUpdateRequest;
import org.store.store.model.Role;
import org.store.store.model.User;
import org.store.store.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SmsService smsService;

    @Test
    public void testGetAllUsers() {
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);
        List<User> users = List.of(user);
        Page<User> expectedPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<User> actual = service.getAllUsers(pageable);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        assertEquals(users, actual.getContent());

        verify(repository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void testGetAllUsersIsEmpty() {
        Page<User> expectedPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<User> actual = service.getAllUsers(pageable);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());

        verify(repository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void testGetUserById() {
        User expected = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);

        when(repository.findById(1)).thenReturn(Optional.of(expected));

        User actual = service.getUserById(1);

        assertNotNull(actual);
        assertEquals(expected, actual);

        verify(repository, times(1)).findById(1);
    }

    @Test
    public void testGetUserByIdNotFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getUserById(99));

        assertEquals(404, ex.getStatusCode().value());

        verify(repository, times(1)).findById(99);
    }

    @Test
    public void testGetUserByNumber() {
        User expected = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);

        when(repository.findByNumber("09111111111")).thenReturn(Optional.of(expected));

        User actual = service.getUserByNumber("09111111111");

        assertNotNull(actual);
        assertEquals(expected, actual);

        verify(repository, times(1)).findByNumber("09111111111");
    }

    @Test
    public void testGetUserByNumberNotFound() {
        when(repository.findByNumber("09342764871")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getUserByNumber("09342764871"));

        assertEquals(404, ex.getStatusCode().value());

        verify(repository, times(1)).findByNumber("09342764871");
    }

    @Test
    public void testAddUser() {
        RegisterRequest input = new RegisterRequest();
        input.setEmail("abcd@gmail.com");
        input.setFirstName("Alireza");
        input.setLastName("Omani");
        input.setNumber("09111111111");
        input.setDateOfBirth("2000-01-01");
        input.setPassword("1234");

        User expected = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);

        when(repository.findByNumber(input.getNumber())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("1234");
        when(repository.save(any(User.class))).thenReturn(expected);

        User actual = service.addUser(input);

        assertEquals(expected.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getNumber(), actual.getNumber());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getFirstName(), actual.getFirstName());

        verify(passwordEncoder, times(1)).encode(any());
        verify(repository, times(1)).findByNumber("09111111111");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    public void testAddUserNumberTaken() {
        RegisterRequest input = new RegisterRequest();
        input.setEmail("abcd@gmail.com");
        input.setFirstName("Alireza");
        input.setLastName("Omani");
        input.setNumber("09111111111");
        input.setDateOfBirth("2000-01-01");
        input.setPassword("1234");

        User existingUser = new User();
        existingUser.setEnabled(true);
        when(repository.findByNumber(input.getNumber())).thenReturn(Optional.of(existingUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.addUser(input));

        assertEquals(400, ex.getStatusCode().value());

        verify(passwordEncoder, never()).encode(any());
        verify(repository, times(1)).findByNumber("09111111111");
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void testEditUserById() {
        UserUpdateRequest updated = new UserUpdateRequest();
        updated.setEmail("efgh@gmail.com");
        updated.setFirstName("Fatemeh");
        updated.setLastName("Has");
        updated.setNumber("09222222222");
        updated.setDateOfBirth("2005-01-01");
        updated.setPassword("5678");
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);

        when(repository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("5678");
        when(repository.existsByNumber(updated.getNumber())).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User actual = service.editUserById(updated, 1);

        assertEquals(updated.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(updated.getEmail(), actual.getEmail());
        assertEquals(updated.getPassword(), actual.getPassword());
        assertEquals(updated.getNumber(), actual.getNumber());
        assertEquals(1, actual.getId());
        assertEquals(updated.getLastName(), actual.getLastName());
        assertEquals(updated.getFirstName(), actual.getFirstName());

        verify(passwordEncoder, times(1)).encode(any());
        verify(repository, times(1)).existsByNumber("09222222222");
        verify(repository, times(1)).save(any(User.class));
        verify(repository, times(1)).findById(1);
    }

    @Test
    public void testEditUserByNumber() {
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);
        UserUpdateRequest updated = new UserUpdateRequest();
        updated.setEmail("efgh@gmail.com");
        updated.setFirstName("Fatemeh");
        updated.setLastName("Has");
        updated.setNumber("09222222222");
        updated.setDateOfBirth("2005-01-01");
        updated.setPassword("5678");

        when(repository.findByNumber("09111111111")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("5678");
        when(repository.existsByNumber(updated.getNumber())).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User actual = service.editUserByNumber(updated, "09111111111");

        assertEquals(updated.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(updated.getEmail(), actual.getEmail());
        assertEquals(updated.getPassword(), actual.getPassword());
        assertEquals(updated.getNumber(), actual.getNumber());
        assertEquals(1, actual.getId());
        assertEquals(updated.getLastName(), actual.getLastName());
        assertEquals(updated.getFirstName(), actual.getFirstName());

        verify(passwordEncoder, times(1)).encode(any());
        verify(repository, times(1)).existsByNumber("09222222222");
        verify(repository, times(1)).save(any(User.class));
        verify(repository, times(1)).findByNumber("09111111111");
    }

    @Test
    public void testEditUserByIdNotFound() {
        UserUpdateRequest updated = new UserUpdateRequest();
        updated.setEmail("efgh@gmail.com");
        updated.setFirstName("Fatemeh");
        updated.setLastName("Has");
        updated.setNumber("09222222222");
        updated.setDateOfBirth("2005-01-01");
        updated.setPassword("5678");

        when(repository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.editUserById(updated, 99));

        assertEquals(404, ex.getStatusCode().value());

        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).existsByNumber("09222222222");
        verify(repository, never()).save(any(User.class));
        verify(repository, times(1)).findById(99);
    }

    @Test
    public void testEditUserByNumberNotFound() {
        UserUpdateRequest updated = new UserUpdateRequest();
        updated.setEmail("efgh@gmail.com");
        updated.setFirstName("Fatemeh");
        updated.setLastName("Has");
        updated.setNumber("09222222222");
        updated.setDateOfBirth("2005-01-01");
        updated.setPassword("5678");

        when(repository.findByNumber("09111111111")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.editUserByNumber(updated, "09111111111"));

        assertEquals(404, ex.getStatusCode().value());

        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).existsByNumber("09222222222");
        verify(repository, never()).save(any(User.class));
        verify(repository, times(1)).findByNumber("09111111111");
    }

    @Test
    public void testEditUserByIdNumberTaken() {
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);
        UserUpdateRequest updated = new UserUpdateRequest();
        updated.setEmail("efgh@gmail.com");
        updated.setFirstName("Fatemeh");
        updated.setLastName("Has");
        updated.setNumber("09222222222");
        updated.setDateOfBirth("2005-01-01");
        updated.setPassword("5678");

        when(repository.findById(1)).thenReturn(Optional.of(user));
        when(repository.existsByNumber(updated.getNumber())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.editUserById(updated, 1));

        assertEquals(400, ex.getStatusCode().value());

        verify(passwordEncoder, never()).encode(any());
        verify(repository, times(1)).existsByNumber("09222222222");
        verify(repository, never()).save(any(User.class));
        verify(repository, times(1)).findById(1);
    }

    @Test
    public void testEditUserByNumberNumberTaken() {
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);
        UserUpdateRequest updated = new UserUpdateRequest();
        updated.setEmail("efgh@gmail.com");
        updated.setFirstName("Fatemeh");
        updated.setLastName("Has");
        updated.setNumber("09222222222");
        updated.setDateOfBirth("2005-01-01");
        updated.setPassword("5678");

        when(repository.findByNumber("09111111111")).thenReturn(Optional.of(user));
        when(repository.existsByNumber(updated.getNumber())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.editUserByNumber(updated, "09111111111"));

        assertEquals(400, ex.getStatusCode().value());

        verify(passwordEncoder, never()).encode(any());
        verify(repository, times(1)).existsByNumber("09222222222");
        verify(repository, never()).save(any(User.class));
        verify(repository, times(1)).findByNumber("09111111111");
    }

    @Test
    public void testDeleteUserById() {
        when(repository.existsById(1)).thenReturn(true);

        doNothing().when(repository).deleteById(1);

        service.deleteUserById(1);

        verify(repository, times(1)).deleteById(1);
        verify(repository, times(1)).existsById(1);
    }

    @Test
    public void testDeleteUserByIdNotFound() {
        when(repository.existsById(100)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteUserById(100));

        assertEquals(404, ex.getStatusCode().value());

        verify(repository, times(1)).existsById(100);
        verify(repository, never()).deleteById(100);
    }

    @Test
    public void testDeleteUserByNumber() {
        when(repository.existsByNumber("09222222222")).thenReturn(true);

        doNothing().when(repository).deleteByNumber("09222222222");

        service.deleteUserByNumber("09222222222");

        verify(repository, times(1)).deleteByNumber("09222222222");
        verify(repository, times(1)).existsByNumber("09222222222");
    }

    @Test
    public void testDeleteUserByNumberNotFound() {
        when(repository.existsByNumber("09222222222")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteUserByNumber("09222222222"));

        assertEquals(404, ex.getStatusCode().value());

        verify(repository, times(1)).existsByNumber("09222222222");
        verify(repository, never()).deleteByNumber("09222222222");
    }

    @Test
    void testSendResetCodeSuccess() {
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);
        user.setEnabled(true);
        when(repository.findByNumber("09111111111")).thenReturn(Optional.of(user));

        service.sendResetCode("09111111111");

        assertNotNull(user.getResetCode());
        assertNotNull(user.getResetCodeExpiry());
        verify(smsService, times(1)).sendSms(eq("09111111111"), anyString());
        verify(repository, times(1)).save(user);
    }

    @Test
    void testResetPasswordSuccess() {
        User user = new User("2000-01-01", "abcd@gmail.com", "Alireza", 1,
                "Omani", "09111111111", "1234", Role.USER);
        user.setResetCode("123456");
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(2));

        ResetPasswordRequest request = new ResetPasswordRequest("09111111111", "123456", "newPass5678");

        when(repository.findByNumber("09111111111")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass5678")).thenReturn("encodedNewPass");

        service.resetPassword(request);

        assertNull(user.getResetCode());
        assertNull(user.getResetCodeExpiry());
        verify(passwordEncoder, times(1)).encode("newPass5678");
        verify(repository, times(1)).save(user);
    }
}