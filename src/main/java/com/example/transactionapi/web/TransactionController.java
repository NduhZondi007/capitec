package com.example.transactionapi.web;

import com.example.transactionapi.model.AppUser;
import com.example.transactionapi.model.Role;
import com.example.transactionapi.repository.AppUserRepository;
import com.example.transactionapi.repository.TransactionRepository;
import com.example.transactionapi.service.TransactionService;
import com.example.transactionapi.web.dto.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller exposing endpoints to retrieve aggregated transaction data.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AppUserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Returns a summary of spending for a specific customer. Users can only
     * access their own data unless they have the ADMIN role.
     */
    @GetMapping("/customers/{customerId}/summary")
    @PreAuthorize("#customerId == principal.id or hasRole('ADMIN')")
    public ResponseEntity<CustomerSummary> getCustomerSummary(@PathVariable Long customerId,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CustomerSummary summary = transactionService.getCustomerSummary(customerId, from, to);
        return ResponseEntity.ok(summary);
    }

    /**
     * Returns overall summary across all customers. Requires ADMIN role.
     */
    @GetMapping("/summary/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OverallSummary> getOverallSummary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        OverallSummary summary = transactionService.getOverallSummary(from, to);
        return ResponseEntity.ok(summary);
    }

    /**
     * Returns a list of top spenders. Requires ADMIN role.
     */
    @GetMapping("/customers/top-spenders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopSpender>> getTopSpenders(@RequestParam(defaultValue = "5") @Min(1) int count,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<TopSpender> topSpenders = transactionService.getTopSpenders(count, from, to);
        return ResponseEntity.ok(topSpenders);
    }

    /**
     * Returns top categories by spend. If customerId is provided it returns
     * categories for that customer. If omitted it returns overall categories.
     * Requires ADMIN role to retrieve overall categories or categories for other users.
     */
    @GetMapping("/categories/top-categories")
    public ResponseEntity<List<TopCategory>> getTopCategories(@RequestParam(defaultValue = "5") @Min(1) int count,
                                                              @RequestParam(required = false) Long customerId,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                              @AuthenticationPrincipal AppUser principal) {
        // Determine access: if no customerId or customerId != principal.id, require admin
        boolean isAdmin = principal.getRole() == Role.ROLE_ADMIN;
        if (customerId == null) {
            if (!isAdmin) {
                return ResponseEntity.status(403).build();
            }
            List<TopCategory> result = transactionService.getTopCategoriesOverall(count, from, to);
            return ResponseEntity.ok(result);
        }
        if (!isAdmin && !customerId.equals(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        List<TopCategory> result = transactionService.getTopCategoriesForCustomer(customerId, count, from, to);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns raw transactions for a customer. Users can view only their own
     * transactions; admins can view any customer's transactions. Pagination
     * could be added via query parameters but is omitted for brevity.
     */
    @GetMapping("/customers/{customerId}/transactions")
    @PreAuthorize("#customerId == principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactions(@PathVariable Long customerId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(transactionRepository.findByCustomerIdAndTimestampBetween(customerId,
                    from.atStartOfDay(), to.atTime(23, 59, 59)));
        } else if (from != null) {
            return ResponseEntity.ok(transactionRepository.findByCustomerIdAndTimestampBetween(customerId,
                    from.atStartOfDay(), LocalDateTime.MAX));
        } else if (to != null) {
            return ResponseEntity.ok(transactionRepository.findByCustomerIdAndTimestampBetween(customerId,
                    LocalDateTime.MIN, to.atTime(23, 59, 59)));
        } else {
            return ResponseEntity.ok(transactionRepository.findByCustomerId(customerId));
        }
    }
}