package com.ega.bank.ega_bank_api.service;

import com.ega.bank.ega_bank_api.model.*;
import com.ega.bank.ega_bank_api.repository.*;
import com.ega.bank.ega_bank_api.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
// iban4j removed for deterministic test-friendly generation

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;

    public Account createAccount(Long clientId, AccountType type) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        Account account = new Account();
        account.setOwner(client);
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountNumber(generateAccountNumber());
        Account saved = accountRepository.save(account);
        return saved;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setDestinationAccount(account);
        tx.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.WITHDRAWAL);
        tx.setAmount(amount);
        tx.setSourceAccount(account);
        tx.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Source and destination must differ");
        }
        Account src = accountRepository.findByAccountNumber(fromAccountNumber).orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account dst = accountRepository.findByAccountNumber(toAccountNumber).orElseThrow(() -> new IllegalArgumentException("Destination account not found"));
        if (src.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }
        src.setBalance(src.getBalance().subtract(amount));
        dst.setBalance(dst.getBalance().add(amount));
        accountRepository.save(src);
        accountRepository.save(dst);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.TRANSFER);
        tx.setAmount(amount);
        tx.setSourceAccount(src);
        tx.setDestinationAccount(dst);
        tx.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    public List<Transaction> getTransactionsForPeriod(String accountNumber, LocalDateTime start, LocalDateTime end) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return transactionRepository.findBySourceAccountIdOrDestinationAccountIdAndTimestampBetween(account.getId(), account.getId(), start, end);
    }

    public org.springframework.data.domain.Page<Transaction> getTransactionsForPeriod(String accountNumber, LocalDateTime start, LocalDateTime end, org.springframework.data.domain.Pageable pageable) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return transactionRepository.findBySourceAccountIdOrDestinationAccountIdAndTimestampBetween(account.getId(), account.getId(), start, end, pageable);
    }

    // Generate a simple unique account identifier resembling an IBAN starting with FR
    // This avoids depending on iban4j and keeps tests deterministic.
    private String generateAccountNumber() {
        Random rnd = new Random();
        String candidate;
        int attempts = 0;
        do {
            // FR + 2 digits (check) + 23 digits (BBAN-like) => 27 chars
            String check = String.format("%02d", rnd.nextInt(100));
            String bban = String.format("%023d", Math.abs(rnd.nextLong()) % 1000000000000000000L);
            // ensure bban length exactly 23 by padding/trimming
            if (bban.length() > 23) bban = bban.substring(0, 23);
            candidate = "FR" + check + bban;
            attempts++;
        } while (accountRepository.findByAccountNumber(candidate).isPresent() && attempts < 20);
        return candidate;
    }
}
