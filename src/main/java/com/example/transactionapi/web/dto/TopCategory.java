package com.example.transactionapi.web.dto;

import com.example.transactionapi.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Represents a category and its total spend, used to list top categories.
 */
@Data
@AllArgsConstructor
public class TopCategory {
    private Category category;
    private BigDecimal totalSpent;
}