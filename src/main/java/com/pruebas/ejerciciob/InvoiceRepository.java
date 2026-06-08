package com.pruebas.ejerciciob;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Invoice> findByIssuedAtBetweenAndType(LocalDateTime start, LocalDateTime end, Invoice.InvoiceType type);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.issuedAt BETWEEN :start AND :end AND i.type = :type")
    BigDecimal sumAmountByIssuedAtBetweenAndType(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("type") Invoice.InvoiceType type);

    @Query(value = "SELECT * FROM invoices WHERE issued_at BETWEEN :start AND :end ORDER BY issued_at DESC LIMIT :lim", nativeQuery = true)
    List<Invoice> findLatestByRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("lim") int lim);
}
