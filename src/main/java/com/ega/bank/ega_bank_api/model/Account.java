package com.ega.bank.ega_bank_api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties("accounts")
    private Client owner;

    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> outgoingTransactions;

    @OneToMany(mappedBy = "destinationAccount", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> incomingTransactions;

}
