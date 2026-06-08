package com.pruebas.ejercicioa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class InterestCalculatorService {

    private static final BigDecimal RATE_TIER1 = new BigDecimal("0.030");
    private static final BigDecimal RATE_TIER2 = new BigDecimal("0.020");
    private static final BigDecimal RATE_TIER3 = new BigDecimal("0.015");
    private static final BigDecimal RATE_TIER4 = new BigDecimal("0.010");

    private static final BigDecimal LIMIT_TIER1 = new BigDecimal("500");
    private static final BigDecimal LIMIT_TIER2 = new BigDecimal("5000");
    private static final BigDecimal LIMIT_TIER3 = new BigDecimal("20000");

    private final LoanRepository repository;

    public InterestCalculatorService(LoanRepository repository) {
        this.repository = repository;
    }

    public BigDecimal calculateInterest(BigDecimal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        if (principal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Principal cannot be negative");
        }
        BigDecimal rate = resolveRate(principal);
        return principal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public LoanRecord calculateAndSave(BigDecimal principal) {
        BigDecimal interest = calculateInterest(principal);
        LoanRecord record = new LoanRecord(principal, interest, LocalDateTime.now());
        return repository.save(record);
    }

    private BigDecimal resolveRate(BigDecimal principal) {
        if (principal.compareTo(LIMIT_TIER1) <= 0) return RATE_TIER1;
        if (principal.compareTo(LIMIT_TIER2) <= 0) return RATE_TIER2;
        if (principal.compareTo(LIMIT_TIER3) <= 0) return RATE_TIER3;
        return RATE_TIER4;
    }
}
