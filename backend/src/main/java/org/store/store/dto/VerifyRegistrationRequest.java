package org.store.store.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyRegistrationRequest {

    @NotBlank
    private String number;

    @NotBlank
    private String code;

    public VerifyRegistrationRequest() {
    }

    public VerifyRegistrationRequest(String number, String code) {
        this.number = number;
        this.code = code;
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
}