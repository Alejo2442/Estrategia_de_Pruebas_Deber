package com.pruebas.ejercicioa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Commission {

    private final BigDecimal baseAmount;
    private final BigDecimal commissionAmount;
    private final LocalDateTime calculatedAt;

    public Commission(BigDecimal baseAmount, BigDecimal commissionAmount, LocalDateTime calculatedAt) {
        this.baseAmount = baseAmount;
        this.commissionAmount = commissionAmount;
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}
