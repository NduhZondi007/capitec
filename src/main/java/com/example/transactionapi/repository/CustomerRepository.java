package com.example.transactionapi.repository;

import com.example.transactionapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Customer entities.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}