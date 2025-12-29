package com.ega.bank.ega_bank_api.repository;

import com.ega.bank.ega_bank_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountIdOrDestinationAccountIdAndTimestampBetween(Long srcId, Long destId, LocalDateTime start, LocalDateTime end);

    Page<Transaction> findBySourceAccountIdOrDestinationAccountIdAndTimestampBetween(Long srcId, Long destId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
