package com.example.transactionapi;

import com.example.transactionapi.service.TransactionService;
import com.example.transactionapi.web.dto.CustomerSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Test
    void testCustomerSummary() {
        // Customer 1 (id 1) should have non-zero spend after data load.
        CustomerSummary summary = transactionService.getCustomerSummary(1L, null, null);
        assertThat(summary.getTotalSpent()).isGreaterThan(BigDecimal.ZERO);
        assertThat(summary.getBreakdown()).isNotEmpty();
    }
}