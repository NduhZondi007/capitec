package com.example.transactionapi.web.dto;

import com.example.transactionapi.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated summary of a customer's spending within a time period. Contains the
 * total amount spent, a breakdown by category and the top spending category.
 */
@Data
@Builder
@AllArgsConstructor
public class CustomerSummary {
    private Long customerId;
    private String periodDescription;
    private BigDecimal totalSpent;
    private List<CategoryBreakdownEntry> breakdown;
    private Category topCategory;
}