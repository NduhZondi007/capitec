package com.example.transactionapi.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Represents a customer and their total spend, used in top spender lists.
 */
@Data
@AllArgsConstructor
public class TopSpender {
    private Long customerId;
    private BigDecimal totalSpent;
}