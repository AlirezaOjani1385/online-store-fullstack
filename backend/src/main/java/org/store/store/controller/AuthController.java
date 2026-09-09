package org.store.store.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.*;
import org.store.store.security.CustomUserDetailsService;
import org.store.store.security.JwtUtils;
import org.store.store.service.UserService;

@RestController
@RequestMapping("/store")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                          CustomUserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Verification code sent to your phone number");
    }

    @PostMapping("/register/verify")
    public ResponseEntity<AuthResponse> verifyRegistration(@Valid @RequestBody VerifyRegistrationRequest request) {
        userService.verifyRegistration(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getNumber());
        String token = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getNumber(), request.getPassword()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not verified. Please verify your phone number.");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid number or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getNumber());
        String token = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<String> requestResetCode(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.sendResetCode(request.getNumber());
        return ResponseEntity.ok("Reset code sent successfully");
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome";
    }
}