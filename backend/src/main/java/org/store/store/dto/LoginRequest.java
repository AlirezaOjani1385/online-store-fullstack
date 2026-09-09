package org.store.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "Number is required")
    @Size(min = 11, max = 11, message = "The size of number must be 11")
    private String number;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "The size of password must be at least 4")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String number, String password) {
        this.number = number;
        this.password = password;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
