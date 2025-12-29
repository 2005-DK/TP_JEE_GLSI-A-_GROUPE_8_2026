package com.ega.bank.ega_bank_api.controller;

import com.ega.bank.ega_bank_api.dto.*;
import com.ega.bank.ega_bank_api.model.*;
import com.ega.bank.ega_bank_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> create(@jakarta.validation.Valid @RequestBody CreateAccountRequest req) {
        Account acc = accountService.createAccount(req.getClientId(), req.getType());
        return ResponseEntity.ok(acc);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> get(@PathVariable String accountNumber) {
        return accountService.findByAccountNumber(accountNumber).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<Transaction> deposit(@PathVariable String accountNumber, @jakarta.validation.Valid @RequestBody TransactionRequest req) {
        Transaction tx = accountService.deposit(accountNumber, req.getAmount());
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Transaction> withdraw(@PathVariable String accountNumber, @jakarta.validation.Valid @RequestBody TransactionRequest req) {
        Transaction tx = accountService.withdraw(accountNumber, req.getAmount());
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@jakarta.validation.Valid @RequestBody TransferRequest req) {
        Transaction tx = accountService.transfer(req.getFromAccount(), req.getToAccount(), req.getAmount());
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<?> transactions(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // If pagination params provided, return a Page; otherwise return full list
        if (page != null || size != null) {
            int p = page != null && page >= 0 ? page : 0;
            int s = size != null && size > 0 ? size : 20;
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(p, s, org.springframework.data.domain.Sort.by("timestamp").descending());
            org.springframework.data.domain.Page<Transaction> txs = accountService.getTransactionsForPeriod(accountNumber, start, end, pageable);
            return ResponseEntity.ok(txs);
        } else {
            return ResponseEntity.ok(accountService.getTransactionsForPeriod(accountNumber, start, end));
        }
    }

    @GetMapping(value = "/{accountNumber}/statement", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> statement(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> txs = accountService.getTransactionsForPeriod(accountNumber, start, end);
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,amount,timestamp,sourceAccount,destinationAccount,description\n");
        for (Transaction t : txs) {
            String src = t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : "";
            String dst = t.getDestinationAccount() != null ? t.getDestinationAccount().getAccountNumber() : "";
            sb.append(t.getId()).append(',')
                    .append(t.getType()).append(',')
                    .append(t.getAmount()).append(',')
                    .append(t.getTimestamp()).append(',')
                    .append(src).append(',')
                    .append(dst).append(',')
                    .append(t.getDescription() != null ? t.getDescription().replace(',', ' ') : "")
                    .append('\n');
        }
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=statement.csv").body(sb.toString());
    }

    @GetMapping(value = "/{accountNumber}/statement.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> statementPdf(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> txs = accountService.getTransactionsForPeriod(accountNumber, start, end);
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(50, 750);
            cs.showText("Statement for account: " + accountNumber);
            cs.endText();

            float y = 730f;
            cs.setFont(PDType1Font.HELVETICA, 10);

            // Header with account info
            Account acc = accountService.findByAccountNumber(accountNumber).orElse(null);
            cs.beginText();
            cs.newLineAtOffset(50, 720);
            String owner = acc != null && acc.getOwner() != null ? acc.getOwner().getFirstName() + " " + acc.getOwner().getLastName() : "";
            cs.showText("Owner: " + owner + "    Balance: " + (acc != null ? acc.getBalance() : "N/A"));
            cs.endText();

            // Column titles
            y -= 20f;
            cs.beginText();
            cs.newLineAtOffset(50, y);
            cs.showText(String.format("%-8s %-12s %-12s %-24s %-16s %-16s", "ID", "TYPE", "AMOUNT", "TIMESTAMP", "SRC", "DST"));
            cs.endText();
            y -= 14f;

            for (Transaction t : txs) {
                if (y < 50f) {
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = 750f;
                }
                cs.beginText();
                cs.newLineAtOffset(50, y);
                String src = t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : "";
                String dst = t.getDestinationAccount() != null ? t.getDestinationAccount().getAccountNumber() : "";
                String line = String.format("%-8s %-12s %-12s %-24s %-16s %-16s", t.getId(), t.getType(), t.getAmount(), t.getTimestamp(), src, dst);
                cs.showText(line);
                cs.endText();
                y -= 14f;
            }
            cs.close();
            doc.save(baos);
            return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=statement.pdf").body(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
