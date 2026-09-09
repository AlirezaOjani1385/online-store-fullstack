package org.store.store.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.store.store.dto.UserUpdateRequest;
import org.store.store.model.User;
import org.store.store.service.UserService;

@RestController
@RequestMapping("/store/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "id") String sortBy,
                                                  @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        User currentUser = userService.getUserByNumber(authentication.getName());
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/by-number")
    public ResponseEntity<User> getUserByNumber(@RequestParam("number") String number) {
        User user = userService.getUserByNumber(number);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(Authentication authentication, @Valid @RequestBody UserUpdateRequest request) {
        User updatedUser = userService.editUserByNumber(request, authentication.getName());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping
    public ResponseEntity<User> editUserByNumber(@Valid @RequestBody UserUpdateRequest request, @RequestParam("number") String number) {
        User u = userService.editUserByNumber(request, number);
        return ResponseEntity.ok(u);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> editUserById(@Valid @RequestBody UserUpdateRequest request, @PathVariable int id) {
        User u = userService.editUserById(request, id);
        return ResponseEntity.ok(u);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        userService.deleteUserByNumber(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable int id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserByNumber(@RequestParam("number") String number) {
        userService.deleteUserByNumber(number);
        return ResponseEntity.noContent().build();
    }
}
