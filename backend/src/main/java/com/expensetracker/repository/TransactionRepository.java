package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTransactionDate(LocalDate date);

    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateAsc(LocalDate start, LocalDate end);

    List<Transaction> findAllByOrderByTransactionDateDesc();
}
