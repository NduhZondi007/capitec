package com.example.transactionapi.web.dto;

import com.example.transactionapi.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated summary across all customers within a time period. Contains the
 * overall total spent, breakdown per category and the top category by
 * expenditure.
 */
@Data
@Builder
@AllArgsConstructor
public class OverallSummary {
    private String periodDescription;
    private BigDecimal totalSpent;
    private List<CategoryBreakdownEntry> breakdown;
    private Category topCategory;
}