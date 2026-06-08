package com.pruebas.ejercicioa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanRecord {

    private final BigDecimal principal;
    private final BigDecimal interestAmount;
    private final LocalDateTime calculatedAt;

    public LoanRecord(BigDecimal principal, BigDecimal interestAmount, LocalDateTime calculatedAt) {
        this.principal = principal;
        this.interestAmount = interestAmount;
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getPrincipal() { return principal; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}
