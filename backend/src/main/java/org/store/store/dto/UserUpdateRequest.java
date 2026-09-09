package org.store.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, message = "The size of first name must be at least 2")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "The size of last name must be at least 2")
    private String lastName;

    @NotBlank(message = "Number is required")
    @Size(min = 11, max = 11, message = "The size of number must be 11")
    private String number;

    @Email(message = "Your email is not valid")
    private String email;

    @Size(min = 4, message = "The size of password must be at least 4")
    private String password;

    private String dateOfBirth;

    public UserUpdateRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
