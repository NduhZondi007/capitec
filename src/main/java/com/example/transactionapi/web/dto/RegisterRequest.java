package com.example.transactionapi.web.dto;

import com.example.transactionapi.model.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for user registration. Role is optional and defaults to USER.
 */
@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private Role role;
}