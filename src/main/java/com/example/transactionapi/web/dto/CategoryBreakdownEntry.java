package com.example.transactionapi.web.dto;

import com.example.transactionapi.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Represents a single category and its aggregated amount.
 */
@Data
@AllArgsConstructor
public class CategoryBreakdownEntry {
    private Category category;
    private BigDecimal total;
}