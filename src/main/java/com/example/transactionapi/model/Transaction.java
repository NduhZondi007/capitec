package com.example.transactionapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a single financial transaction. Each transaction belongs
 * to a customer and is assigned a spending category during ingestion. The
 * merchantCategoryCode (MCC) and description fields are preserved for
 * transparency and to support further enrichment.
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier from the source system (e.g. external transaction id). It may
     * not be unique across all data sources so is stored separately from the
     * primary key.
     */
    private String externalId;

    private LocalDateTime timestamp;

    private String description;

    private String merchant;

    private String merchantCategoryCode;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}