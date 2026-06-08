package com.pruebas.ejerciciob;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TransactionRepository — H2 Integration Tests")
class TransactionRepositoryH2Test {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TransactionRepository repository;

    private final LocalDateTime BASE = LocalDateTime.of(2024, 5, 10, 8, 0);

    @Test
    @DisplayName("findByIssuedAtBetween returns transactions within the date range")
    void findByIssuedAtBetweenReturnsMatchingTransactions() {
        em.persist(new Transaction(new BigDecimal("200.00"), "Web design",      BASE,              Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("80.00"),  "Office supplies", BASE.plusDays(1),  Transaction.TransactionType.EXPENSE));
        em.persist(new Transaction(new BigDecimal("450.00"), "Consulting",      BASE.plusDays(3),  Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("130.00"), "Travel",          BASE.plusDays(4),  Transaction.TransactionType.EXPENSE));
        em.persist(new Transaction(new BigDecimal("999.00"), "Future",          BASE.plusMonths(1), Transaction.TransactionType.INCOME));
        em.flush();

        List<Transaction> result = repository.findByIssuedAtBetween(BASE, BASE.plusDays(4));

        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("findByIssuedAtBetween returns empty list when no transactions match")
    void findByIssuedAtBetweenReturnsEmptyWhenNoMatch() {
        em.persist(new Transaction(new BigDecimal("100.00"), "Old",  BASE.minusMonths(3), Transaction.TransactionType.EXPENSE));
        em.flush();

        List<Transaction> result = repository.findByIssuedAtBetween(BASE, BASE.plusDays(10));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIssuedAtBetweenAndType returns only INCOME transactions")
    void findByIssuedAtBetweenAndTypeFiltersIncome() {
        em.persist(new Transaction(new BigDecimal("500.00"), "Product sale", BASE,              Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("120.00"), "Rent",         BASE.plusDays(1), Transaction.TransactionType.EXPENSE));
        em.persist(new Transaction(new BigDecimal("300.00"), "Service fee",  BASE.plusDays(2), Transaction.TransactionType.INCOME));
        em.flush();

        List<Transaction> income = repository.findByIssuedAtBetweenAndType(BASE, BASE.plusDays(2), Transaction.TransactionType.INCOME);

        assertThat(income).hasSize(2);
        assertThat(income).allMatch(t -> t.getType() == Transaction.TransactionType.INCOME);
    }

    @Test
    @DisplayName("findByIssuedAtBetweenAndType returns only EXPENSE transactions")
    void findByIssuedAtBetweenAndTypeFiltersExpense() {
        em.persist(new Transaction(new BigDecimal("500.00"), "Product sale", BASE,              Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("120.00"), "Rent",         BASE.plusDays(1), Transaction.TransactionType.EXPENSE));
        em.persist(new Transaction(new BigDecimal("300.00"), "Service fee",  BASE.plusDays(2), Transaction.TransactionType.INCOME));
        em.flush();

        List<Transaction> expenses = repository.findByIssuedAtBetweenAndType(BASE, BASE.plusDays(2), Transaction.TransactionType.EXPENSE);

        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).getAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType sums INCOME correctly")
    void sumAmountSumsIncomeCorrectly() {
        em.persist(new Transaction(new BigDecimal("500.00"), "Sale A", BASE,              Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("120.00"), "Cost A", BASE,              Transaction.TransactionType.EXPENSE));
        em.persist(new Transaction(new BigDecimal("300.00"), "Sale B", BASE.plusDays(1),  Transaction.TransactionType.INCOME));
        em.flush();

        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(BASE, BASE.plusDays(1), Transaction.TransactionType.INCOME);

        assertThat(total).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType returns 0 when no transactions match")
    void sumAmountReturnsZeroWhenNoMatch() {
        em.persist(new Transaction(new BigDecimal("200.00"), "Sale", BASE, Transaction.TransactionType.INCOME));
        em.flush();

        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(
                BASE.plusDays(30), BASE.plusDays(60), Transaction.TransactionType.INCOME);

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("findLatestByRange returns limited results ordered by most recent first")
    void findLatestByRangeReturnsLimitedResultsDescending() {
        em.persist(new Transaction(new BigDecimal("100.00"), "First",  BASE,             Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("200.00"), "Second", BASE.plusDays(1), Transaction.TransactionType.INCOME));
        em.persist(new Transaction(new BigDecimal("300.00"), "Third",  BASE.plusDays(2), Transaction.TransactionType.INCOME));
        em.flush();

        List<Transaction> result = repository.findLatestByRange(BASE, BASE.plusDays(2), 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getConcept()).isEqualTo("Third");
    }
}
