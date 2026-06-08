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
@DisplayName("TransactionRepository — PostgreSQL Integration Tests (Testcontainers)")
class TransactionRepositoryPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TransactionRepository repository;

    private final LocalDateTime BASE = LocalDateTime.of(2024, 7, 1, 9, 0);

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new Transaction(new BigDecimal("1500.00"), "Annual license",   BASE,              Transaction.TransactionType.INCOME),
                new Transaction(new BigDecimal("400.00"),  "Server hosting",   BASE.plusDays(2),  Transaction.TransactionType.EXPENSE),
                new Transaction(new BigDecimal("900.00"),  "Support contract", BASE.plusDays(4),  Transaction.TransactionType.INCOME),
                new Transaction(new BigDecimal("250.00"),  "Office supplies",  BASE.plusDays(6),  Transaction.TransactionType.EXPENSE),
                new Transaction(new BigDecimal("600.00"),  "Out of range",     BASE.plusMonths(2), Transaction.TransactionType.INCOME)
        ));
    }

    @Test
    @DisplayName("findByIssuedAtBetween returns correct transactions on real PostgreSQL")
    void findByIssuedAtBetweenWorksOnPostgres() {
        List<Transaction> result = repository.findByIssuedAtBetween(BASE, BASE.plusDays(6));
        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("findByIssuedAtBetweenAndType returns only INCOME transactions on PostgreSQL")
    void findByIssuedAtBetweenAndTypeReturnsIncomeOnPostgres() {
        List<Transaction> income = repository.findByIssuedAtBetweenAndType(BASE, BASE.plusDays(6), Transaction.TransactionType.INCOME);
        assertThat(income).hasSize(2);
        assertThat(income).allMatch(t -> t.getType() == Transaction.TransactionType.INCOME);
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType sums INCOME correctly on PostgreSQL")
    void sumIncomeCorrectlyOnPostgres() {
        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(BASE, BASE.plusDays(6), Transaction.TransactionType.INCOME);
        assertThat(total).isEqualByComparingTo("2400.00");
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType sums EXPENSE correctly on PostgreSQL")
    void sumExpenseCorrectlyOnPostgres() {
        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(BASE, BASE.plusDays(6), Transaction.TransactionType.EXPENSE);
        assertThat(total).isEqualByComparingTo("650.00");
    }

    @Test
    @DisplayName("findLatestByRange with LIMIT works on real PostgreSQL dialect")
    void findLatestByRangeWorksOnPostgres() {
        List<Transaction> result = repository.findLatestByRange(BASE, BASE.plusDays(6), 3);
        assertThat(result).hasSize(3);
    }
}
