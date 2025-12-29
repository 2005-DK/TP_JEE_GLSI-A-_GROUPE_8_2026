package com.ega.bank.ega_bank_api.service;

import com.ega.bank.ega_bank_api.model.Client;
import com.ega.bank.ega_bank_api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Client create(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    public Client update(Long id, Client updated) {
        Client existing = clientRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setBirthDate(updated.getBirthDate());
        existing.setGender(updated.getGender());
        existing.setAddress(updated.getAddress());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setNationality(updated.getNationality());
        return clientRepository.save(existing);
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }
}
