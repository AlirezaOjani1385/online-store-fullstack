package org.store.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CheckoutRequest {

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Postal Code is required")
    @Pattern(regexp = "^\\d{10}$", message = "The postal code must have 10 digits")
    private String postalCode;

    public CheckoutRequest() {
    }

    public CheckoutRequest(String address, String postalCode) {
        this.address = address;
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}