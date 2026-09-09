package org.store.store.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {

    @NotBlank(message = "Number is required")
    private String number;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "New password is required")
    private String newPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String number, String code, String newPassword) {
        this.number = number;
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}