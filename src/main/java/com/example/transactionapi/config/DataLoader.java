package com.example.transactionapi.config;

import com.example.transactionapi.model.Category;
import com.example.transactionapi.model.Customer;
import com.example.transactionapi.model.Transaction;
import com.example.transactionapi.repository.CustomerRepository;
import com.example.transactionapi.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Loads transaction data from a CSV file on application startup. The CSV file
 * should be located in the classpath and contain records with columns:
 * externalId,customerName,customerEmail,timestamp,description,merchant,mcc,amount,category
 * where category is optional. Customers will be created on the fly if not
 * already present. The loader performs a simple keyword-based categorization
 * when the CSV does not provide a category.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Value("classpath:data/transactions.csv")
    private Resource transactionsCsv;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, Category> keywordCategoryMap = new HashMap<>();

    @PostConstruct
    public void loadData() throws IOException {
        initializeKeywordMap();
        Map<String, Customer> customerCache = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(transactionsCsv.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Skip header line
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 8) {
                    log.warn("Skipping invalid line: {}", line);
                    continue;
                }
                String externalId = parts[0].trim();
                String customerName = parts[1].trim();
                String customerEmail = parts[2].trim();
                String timestampStr = parts[3].trim();
                String description = parts[4].trim();
                String merchant = parts[5].trim();
                String mcc = parts[6].trim();
                String amountStr = parts[7].trim();
                String categoryStr = parts.length > 8 ? parts[8].trim() : "";

                Customer customer = customerCache.get(customerEmail);
                if (customer == null) {
                    customer = Customer.builder()
                            .name(customerName)
                            .email(customerEmail)
                            .build();
                    customer = customerRepository.save(customer);
                    customerCache.put(customerEmail, customer);
                }

                LocalDateTime timestamp;
                try {
                    timestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
                } catch (Exception e) {
                    log.warn("Invalid timestamp '{}', skipping", timestampStr);
                    continue;
                }

                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid amount '{}', skipping", amountStr);
                    continue;
                }

                Category category;
                if (!categoryStr.isEmpty()) {
                    try {
                        category = Category.valueOf(categoryStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        category = categorize(description, merchant, mcc);
                    }
                } else {
                    category = categorize(description, merchant, mcc);
                }

                Transaction transaction = Transaction.builder()
                        .externalId(externalId)
                        .timestamp(timestamp)
                        .description(description)
                        .merchant(merchant)
                        .merchantCategoryCode(mcc)
                        .amount(amount)
                        .category(category)
                        .customer(customer)
                        .build();
                transactionRepository.save(transaction);
            }
        }
        log.info("Data loading complete: {} customers, {} transactions", customerRepository.count(), transactionRepository.count());
    }

    private void initializeKeywordMap() {
        // Map keywords to categories. Keys are lower-case.
        // Food / Groceries
        addKeywords(Category.FOOD, Arrays.asList("grocery", "market", "supermarket", "food", "restaurant", "cafe", "coffee"));
        // Transport
        addKeywords(Category.TRANSPORT, Arrays.asList("uber", "taxi", "bus", "train", "fuel", "gas station", "petrol", "subway", "transport"));
        // Utilities
        addKeywords(Category.UTILITIES, Arrays.asList("electric", "gas", "water", "utility", "power", "energy"));
        // Entertainment
        addKeywords(Category.ENTERTAINMENT, Arrays.asList("cinema", "movie", "netflix", "theatre", "concert", "entertainment", "game"));
        // Shopping
        addKeywords(Category.SHOPPING, Arrays.asList("shop", "store", "mall", "clothes", "amazon", "ecommerce", "retail"));
        // Healthcare
        addKeywords(Category.HEALTHCARE, Arrays.asList("pharmacy", "doctor", "hospital", "clinic", "medicine", "dentist"));
        // Communication
        addKeywords(Category.COMMUNICATION, Arrays.asList("phone", "internet", "cell", "mobile", "telecom", "data"));
        // Education
        addKeywords(Category.EDUCATION, Arrays.asList("school", "university", "tuition", "course", "college", "education"));
        // Travel
        addKeywords(Category.TRAVEL, Arrays.asList("flight", "airline", "hotel", "travel", "air", "booking", "airbnb"));
        // Income
        addKeywords(Category.INCOME, Arrays.asList("salary", "payroll", "deposit", "income", "bonus"));
    }

    private void addKeywords(Category category, List<String> keywords) {
        for (String k : keywords) {
            keywordCategoryMap.put(k.toLowerCase(), category);
        }
    }

    private Category categorize(String description, String merchant, String mcc) {
        String combined = (description + " " + merchant + " " + mcc).toLowerCase();
        for (Map.Entry<String, Category> entry : keywordCategoryMap.entrySet()) {
            if (combined.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Category.OTHER;
    }
}