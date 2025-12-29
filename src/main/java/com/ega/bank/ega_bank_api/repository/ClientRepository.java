package com.ega.bank.ega_bank_api.repository;

import com.ega.bank.ega_bank_api.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
