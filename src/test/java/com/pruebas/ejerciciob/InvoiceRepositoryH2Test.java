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
@DisplayName("InvoiceRepository — H2 Integration Tests")
class InvoiceRepositoryH2Test {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InvoiceRepository repository;

    private final LocalDateTime BASE = LocalDateTime.of(2024, 5, 10, 8, 0);

    @Test
    @DisplayName("findByIssuedAtBetween returns invoices within the date range")
    void findByIssuedAtBetweenReturnsMatchingInvoices() {
        em.persist(new Invoice(new BigDecimal("200.00"), "Web design", BASE, Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("80.00"),  "Office supplies", BASE.plusDays(1), Invoice.InvoiceType.EXPENSE));
        em.persist(new Invoice(new BigDecimal("450.00"), "Consulting", BASE.plusDays(3), Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("130.00"), "Travel", BASE.plusDays(4), Invoice.InvoiceType.EXPENSE));
        em.persist(new Invoice(new BigDecimal("999.00"), "Future invoice", BASE.plusMonths(1), Invoice.InvoiceType.INCOME));
        em.flush();

        List<Invoice> result = repository.findByIssuedAtBetween(BASE, BASE.plusDays(4));

        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("findByIssuedAtBetween returns empty list when no invoices match")
    void findByIssuedAtBetweenReturnsEmptyWhenNoMatch() {
        em.persist(new Invoice(new BigDecimal("100.00"), "Old invoice", BASE.minusMonths(3), Invoice.InvoiceType.EXPENSE));
        em.flush();

        List<Invoice> result = repository.findByIssuedAtBetween(BASE, BASE.plusDays(10));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIssuedAtBetweenAndType returns only INCOME invoices")
    void findByIssuedAtBetweenAndTypeFiltersIncome() {
        em.persist(new Invoice(new BigDecimal("500.00"), "Product sale", BASE, Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("120.00"), "Rent", BASE.plusDays(1), Invoice.InvoiceType.EXPENSE));
        em.persist(new Invoice(new BigDecimal("300.00"), "Service fee", BASE.plusDays(2), Invoice.InvoiceType.INCOME));
        em.flush();

        List<Invoice> income = repository.findByIssuedAtBetweenAndType(BASE, BASE.plusDays(2), Invoice.InvoiceType.INCOME);

        assertThat(income).hasSize(2);
        assertThat(income).allMatch(inv -> inv.getType() == Invoice.InvoiceType.INCOME);
    }

    @Test
    @DisplayName("findByIssuedAtBetweenAndType returns only EXPENSE invoices")
    void findByIssuedAtBetweenAndTypeFiltersExpense() {
        em.persist(new Invoice(new BigDecimal("500.00"), "Product sale", BASE, Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("120.00"), "Rent", BASE.plusDays(1), Invoice.InvoiceType.EXPENSE));
        em.persist(new Invoice(new BigDecimal("300.00"), "Service fee", BASE.plusDays(2), Invoice.InvoiceType.INCOME));
        em.flush();

        List<Invoice> expenses = repository.findByIssuedAtBetweenAndType(BASE, BASE.plusDays(2), Invoice.InvoiceType.EXPENSE);

        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).getAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType sums INCOME correctly")
    void sumAmountSumsIncomeCorrectly() {
        em.persist(new Invoice(new BigDecimal("500.00"), "Sale A", BASE, Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("120.00"), "Cost A", BASE, Invoice.InvoiceType.EXPENSE));
        em.persist(new Invoice(new BigDecimal("300.00"), "Sale B", BASE.plusDays(1), Invoice.InvoiceType.INCOME));
        em.flush();

        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(BASE, BASE.plusDays(1), Invoice.InvoiceType.INCOME);

        assertThat(total).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("sumAmountByIssuedAtBetweenAndType returns 0 when no invoices match")
    void sumAmountReturnsZeroWhenNoMatch() {
        em.persist(new Invoice(new BigDecimal("200.00"), "Sale", BASE, Invoice.InvoiceType.INCOME));
        em.flush();

        BigDecimal total = repository.sumAmountByIssuedAtBetweenAndType(
                BASE.plusDays(30), BASE.plusDays(60), Invoice.InvoiceType.INCOME);

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("findLatestByRange returns limited results ordered by most recent first")
    void findLatestByRangeReturnsLimitedResultsDescending() {
        em.persist(new Invoice(new BigDecimal("100.00"), "First",  BASE,              Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("200.00"), "Second", BASE.plusDays(1),  Invoice.InvoiceType.INCOME));
        em.persist(new Invoice(new BigDecimal("300.00"), "Third",  BASE.plusDays(2),  Invoice.InvoiceType.INCOME));
        em.flush();

        List<Invoice> result = repository.findLatestByRange(BASE, BASE.plusDays(2), 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getConcept()).isEqualTo("Third");
    }
}
