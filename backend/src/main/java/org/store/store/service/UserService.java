package org.store.store.service;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.store.store.dto.RegisterRequest;
import org.store.store.dto.ResetPasswordRequest;
import org.store.store.dto.UserUpdateRequest;
import org.store.store.dto.VerifyRegistrationRequest;
import org.store.store.model.Role;
import org.store.store.model.User;
import org.store.store.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, SmsService smsService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.smsService = smsService;
    }

    @NonNull
    private User applyUpdate(UserUpdateRequest updated, User user) {
        user.setNumber(updated.getNumber());
        user.setEmail(updated.getEmail());
        user.setDateOfBirth(updated.getDateOfBirth());
        user.setFirstName(updated.getFirstName());
        user.setLastName(updated.getLastName());

        if (updated.getPassword() != null && !updated.getPassword().trim().isEmpty())
            user.setPassword(passwordEncoder.encode(updated.getPassword()));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserByNumber(String number) {
        return userRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User addUser(RegisterRequest request) {
        User user = userRepository.findByNumber(request.getNumber()).orElse(null);

        if (user != null && user.isEnabled())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number is already taken");

        if (user == null) {
            user = new User();
            user.setRole(Role.USER);
            user.setEnabled(false);
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setNumber(request.getNumber());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(2));

        User savedUser = userRepository.save(user);

        smsService.sendSms(savedUser.getNumber(), "Your registration verification code: " + code);

        return savedUser;
    }

    @Transactional
    public void verifyRegistration(VerifyRegistrationRequest request) {
        User user = userRepository.findByNumber(request.getNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEnabled())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already verified");

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code");

        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code has expired");

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);

        userRepository.save(user);
    }

    @Transactional
    public User editUserById(UserUpdateRequest updated, int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getNumber().equals(updated.getNumber()) && userRepository.existsByNumber(updated.getNumber()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number is already taken");

        return applyUpdate(updated, user);
    }

    @Transactional
    public User editUserByNumber(UserUpdateRequest updated, String number) {
        User user = userRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getNumber().equals(updated.getNumber()) && userRepository.existsByNumber(updated.getNumber()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number is already taken");

        return applyUpdate(updated, user);
    }

    @Transactional
    public void deleteUserById(int id) {
        if (!userRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteUserByNumber(String number) {
        if (!userRepository.existsByNumber(number))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        userRepository.deleteByNumber(number);
    }

    @Transactional
    public void sendResetCode(String number) {
        User user = userRepository.findByNumber(number)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isEnabled())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not verified");

        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(2));
        userRepository.save(user);

        smsService.sendSms(user.getNumber(), "Your verification code: " + code);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByNumber(request.getNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getResetCode() == null || !user.getResetCode().equals(request.getCode()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset code");

        if (user.getResetCodeExpiry().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset code has expired");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetCode(null);
        user.setResetCodeExpiry(null);

        userRepository.save(user);
    }
}