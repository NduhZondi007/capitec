package com.example.transactionapi.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for user authentication.
 */
@Data
public class AuthenticationRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}