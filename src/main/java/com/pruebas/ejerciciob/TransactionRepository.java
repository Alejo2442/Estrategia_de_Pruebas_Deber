package com.pruebas.ejerciciob;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByIssuedAtBetweenAndType(LocalDateTime start, LocalDateTime end, Transaction.TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.issuedAt BETWEEN :start AND :end AND t.type = :type")
    BigDecimal sumAmountByIssuedAtBetweenAndType(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("type") Transaction.TransactionType type);

    @Query(value = "SELECT * FROM transactions WHERE issued_at BETWEEN :start AND :end ORDER BY issued_at DESC LIMIT :lim", nativeQuery = true)
    List<Transaction> findLatestByRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("lim") int lim);
}
