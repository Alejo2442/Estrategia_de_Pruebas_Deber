package com.pruebas.ejercicioa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class CommissionService {

    private static final BigDecimal RATE_TIER1 = new BigDecimal("0.030");
    private static final BigDecimal RATE_TIER2 = new BigDecimal("0.020");
    private static final BigDecimal RATE_TIER3 = new BigDecimal("0.015");
    private static final BigDecimal RATE_TIER4 = new BigDecimal("0.010");

    private static final BigDecimal LIMIT_TIER1 = new BigDecimal("500");
    private static final BigDecimal LIMIT_TIER2 = new BigDecimal("5000");
    private static final BigDecimal LIMIT_TIER3 = new BigDecimal("20000");

    private final CommissionRepository repository;

    public CommissionService(CommissionRepository repository) {
        this.repository = repository;
    }

    public BigDecimal calculateCommission(BigDecimal baseAmount) {
        if (baseAmount == null) {
            throw new IllegalArgumentException("baseAmount cannot be null");
        }
        if (baseAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("baseAmount cannot be negative");
        }
        BigDecimal rate = resolveRate(baseAmount);
        return baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public Commission calculateAndSave(BigDecimal baseAmount) {
        BigDecimal commissionAmount = calculateCommission(baseAmount);
        Commission commission = new Commission(baseAmount, commissionAmount, LocalDateTime.now());
        return repository.save(commission);
    }

    private BigDecimal resolveRate(BigDecimal baseAmount) {
        if (baseAmount.compareTo(LIMIT_TIER1) <= 0) return RATE_TIER1;
        if (baseAmount.compareTo(LIMIT_TIER2) <= 0) return RATE_TIER2;
        if (baseAmount.compareTo(LIMIT_TIER3) <= 0) return RATE_TIER3;
        return RATE_TIER4;
    }
}
