package com.example.transactionapi.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response body containing the issued JWT token.
 */
@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
}