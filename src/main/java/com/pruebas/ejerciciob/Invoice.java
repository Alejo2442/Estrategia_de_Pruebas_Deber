package com.pruebas.ejerciciob;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String concept;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    protected Invoice() {}

    public Invoice(BigDecimal amount, String concept, LocalDateTime issuedAt, InvoiceType type) {
        this.amount = amount;
        this.concept = concept;
        this.issuedAt = issuedAt;
        this.type = type;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getConcept() { return concept; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public InvoiceType getType() { return type; }

    public enum InvoiceType {
        INCOME, EXPENSE
    }
}
