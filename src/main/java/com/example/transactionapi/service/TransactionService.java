package com.example.transactionapi.service;

import com.example.transactionapi.model.Category;
import com.example.transactionapi.model.Customer;
import com.example.transactionapi.repository.CustomerRepository;
import com.example.transactionapi.repository.TransactionRepository;
import com.example.transactionapi.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides aggregation logic for transactions. Uses repository queries to
 * compute sums grouped by category or customer. This service abstracts the
 * underlying data access and transforms raw results into descriptive DTOs.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    /**
     * Returns a summary of a customer's spending between the optional date range.
     * If from or to is null the range is unbounded on that side.
     */
    @Transactional(readOnly = true)
    public CustomerSummary getCustomerSummary(Long customerId, LocalDate from, LocalDate to) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;
        List<Object[]> results = transactionRepository.sumAmountByCustomerAndCategory(customerId, fromDateTime, toDateTime);
        List<CategoryBreakdownEntry> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Category topCategory = null;
        BigDecimal topValue = BigDecimal.ZERO;
        for (Object[] row : results) {
            Category category = (Category) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            breakdown.add(new CategoryBreakdownEntry(category, sum));
            total = total.add(sum);
            if (sum.compareTo(topValue) > 0) {
                topValue = sum;
                topCategory = category;
            }
        }
        // In case there are no transactions, set topCategory to null
        String periodDesc = buildPeriodDescription(from, to);
        return CustomerSummary.builder()
                .customerId(customerId)
                .periodDescription(periodDesc)
                .totalSpent(total)
                .breakdown(breakdown)
                .topCategory(topCategory)
                .build();
    }

    /**
     * Returns an overall summary across all customers.
     */
    @Transactional(readOnly = true)
    public OverallSummary getOverallSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;
        List<Object[]> results = transactionRepository.sumAmountByCategory(fromDateTime, toDateTime);
        List<CategoryBreakdownEntry> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Category topCategory = null;
        BigDecimal topValue = BigDecimal.ZERO;
        for (Object[] row : results) {
            Category category = (Category) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            breakdown.add(new CategoryBreakdownEntry(category, sum));
            total = total.add(sum);
            if (sum.compareTo(topValue) > 0) {
                topValue = sum;
                topCategory = category;
            }
        }
        String periodDesc = buildPeriodDescription(from, to);
        return OverallSummary.builder()
                .periodDescription(periodDesc)
                .totalSpent(total)
                .breakdown(breakdown)
                .topCategory(topCategory)
                .build();
    }

    /**
     * Returns a list of the top N customers by spending within an optional date range.
     */
    @Transactional(readOnly = true)
    public List<TopSpender> getTopSpenders(int count, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;
        List<Object[]> results = transactionRepository.sumAmountPerCustomer(fromDateTime, toDateTime);
        return results.stream()
                .limit(count)
                .map(row -> new TopSpender((Long) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());
    }

    /**
     * Returns the top categories for a given customer.
     */
    @Transactional(readOnly = true)
    public List<TopCategory> getTopCategoriesForCustomer(Long customerId, int count, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;
        List<Object[]> results = transactionRepository.topCategoriesForCustomer(customerId, fromDateTime, toDateTime);
        return results.stream()
                .limit(count)
                .map(row -> new TopCategory((Category) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());
    }

    /**
     * Returns the top categories across all customers.
     */
    @Transactional(readOnly = true)
    public List<TopCategory> getTopCategoriesOverall(int count, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;
        List<Object[]> results = transactionRepository.topCategoriesOverall(fromDateTime, toDateTime);
        return results.stream()
                .limit(count)
                .map(row -> new TopCategory((Category) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());
    }

    private String buildPeriodDescription(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return "All time";
        } else if (from != null && to != null) {
            return String.format("%s to %s", from, to);
        } else if (from != null) {
            return String.format("From %s", from);
        } else {
            return String.format("Until %s", to);
        }
    }
}