package com.example.transactionapi.model;

/**
 * Roles available in the system. A regular customer will have USER role and
 * administrators will have ADMIN role allowing broader access to aggregated
 * data. Spring Security prefixes roles with "ROLE_" by convention.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}