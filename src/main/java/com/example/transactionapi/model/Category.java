package com.example.transactionapi.model;

/**
 * Enumeration of broad spending categories relevant to retail banking customers.
 * These categories cover common personal finance groups such as groceries,
 * transport and utilities. Transactions will be assigned to one of these
 * categories during ingestion based on merchant information or keywords.
 */
public enum Category {
    FOOD,
    TRANSPORT,
    UTILITIES,
    ENTERTAINMENT,
    SHOPPING,
    HEALTHCARE,
    COMMUNICATION,
    EDUCATION,
    TRAVEL,
    INCOME,
    OTHER
}