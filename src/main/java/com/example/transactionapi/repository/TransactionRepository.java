package com.example.transactionapi.repository;

import com.example.transactionapi.model.Category;
import com.example.transactionapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for transaction entities. Contains custom queries for aggregating
 * amounts by category and customer, as well as identifying top spenders.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerId(Long customerId);

    List<Transaction> findByCustomerIdAndTimestampBetween(Long customerId, LocalDateTime from, LocalDateTime to);

    List<Transaction> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Returns the total spend per category for a given customer within a date range.
     * Returns a list of Object[] where index 0 is the Category and index 1 is the sum amount (BigDecimal).
     */
    @Query("SELECT t.category as category, SUM(t.amount) as total " +
            "FROM Transaction t WHERE t.customer.id = :customerId " +
            "AND (:from IS NULL OR t.timestamp >= :from) " +
            "AND (:to IS NULL OR t.timestamp <= :to) " +
            "GROUP BY t.category")
    List<Object[]> sumAmountByCustomerAndCategory(@Param("customerId") Long customerId,
                                                @Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    /**
     * Returns the total spend per category across all customers within a date range.
     */
    @Query("SELECT t.category as category, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE (:from IS NULL OR t.timestamp >= :from) " +
            "AND (:to IS NULL OR t.timestamp <= :to) " +
            "GROUP BY t.category")
    List<Object[]> sumAmountByCategory(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    /**
     * Returns total spend per customer within a date range.
     * Each element: [customerId (Long), totalSpent (BigDecimal)]
     */
    @Query("SELECT t.customer.id as customerId, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE (:from IS NULL OR t.timestamp >= :from) " +
            "AND (:to IS NULL OR t.timestamp <= :to) " +
            "GROUP BY t.customer.id ORDER BY total DESC")
    List<Object[]> sumAmountPerCustomer(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);

    /**
     * Returns total spend per category for a specific customer, ordered by total descending.
     * Each element: [category (Category), totalSpent (BigDecimal)]
     */
    @Query("SELECT t.category as category, SUM(t.amount) as total " +
            "FROM Transaction t WHERE t.customer.id = :customerId " +
            "AND (:from IS NULL OR t.timestamp >= :from) " +
            "AND (:to IS NULL OR t.timestamp <= :to) " +
            "GROUP BY t.category ORDER BY total DESC")
    List<Object[]> topCategoriesForCustomer(@Param("customerId") Long customerId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    /**
     * Returns total spend per category across all customers, ordered by total descending.
     */
    @Query("SELECT t.category as category, SUM(t.amount) as total " +
            "FROM Transaction t " +
            "WHERE (:from IS NULL OR t.timestamp >= :from) " +
            "AND (:to IS NULL OR t.timestamp <= :to) " +
            "GROUP BY t.category ORDER BY total DESC")
    List<Object[]> topCategoriesOverall(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);
}