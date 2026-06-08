package com.pruebas.ejerciciob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@DisplayName("InvoiceRepository — PostgreSQL Integration Tests (Testcontainers)")
class InvoiceRepositoryPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private InvoiceRepository repository;

    private final LocalDateTime BASE = LocalDateTime.of(2024, 7, 1, 9, 0);

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new Invoice(new BigDecimal("1500.00"), "Annual license",   BASE,              Invoice.InvoiceType.INCOME),
                new Invoice(new BigDecimal("400.00"),  "Server hosting",   BASE.plusDays(2),  Invoice.InvoiceType.EXPENSE),
                new Invoice(new BigDecimal("900.00"),  "Support contract", BASE.plusDays(4),  Invoice.InvoiceType.INCOME),
                new Invoice(new BigDecimal("250.00"),  "Office supplies",  BASE.plusDays(6),  Invoice.InvoiceType.EXPENSE),
                new Invoice(new BigDecimal("600.00"),  "Out of range",     BASE.plusMonths(2), Invoice.InvoiceType.INCOME)
        ));
    }

    @Test
    @DisplayName("findByIssuedAtBetween returns correct invoices on real PostgreSQL")
    void findByIssuedAtBetweenWorksOnPostgres() {
        List<Invoice> result = repository.findByIssuedAtBetween(BASE, BASE.plusDays(6));
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("findByIssuedAtBetweenAndType returns only INCOME invoices on PostgreSQL")
    void findByIssuedAtBetweenAndTypeReturnsIncomeOnPostgres() {
        List<Invoice> income = repository.findByIssuedAtBetweenAndType(BASE, BASE.plusDays(6), Invoice.InvoiceType.INCOME);
        assertThat(income).hasSize(2);
        assertThat(income).allMatch(inv -> inv.getType() == Invoice.InvoiceType.INCOME);
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType sums INCOME correctly on PostgreSQL")
    void sumIncomeCorrectlyOnPostgres() {
        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(BASE, BASE.plusDays(6), Invoice.InvoiceType.INCOME);
        assertThat(total).isEqualByComparingTo("2400.00");
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType sums EXPENSE correctly on PostgreSQL")
    void sumExpenseCorrectlyOnPostgres() {
        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(BASE, BASE.plusDays(6), Invoice.InvoiceType.EXPENSE);
        assertThat(total).isEqualByComparingTo("650.00");
    }

    @Test
    @DisplayName("findLatestByRange with LIMIT works on real PostgreSQL dialect")
    void findLatestByRangeWorksOnPostgres() {
        List<Invoice> result = repository.findLatestByRange(BASE, BASE.plusDays(6), 3);
        assertThat(result).hasSize(3);
    }
}
