package com.freightops.service;

import com.freightops.entity.Client;
import com.freightops.dto.ClientRequest;
import com.freightops.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Client Service
 * Business logic for Client operations
 */
@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Create a new client
     * @param clientRequest Client data transfer object
     * @return created Client
     */
    public Client createClient(ClientRequest clientRequest) {
        // Convert DTO to Entity
        Client client = new Client();
        client.setName(clientRequest.getName());
        client.setAddress(clientRequest.getAddress());
        client.setContactNumber(clientRequest.getContactNumber());

        return clientRepository.save(client);
    }

    /**
     * Get client by ID
     * @param id Client ID
     * @return Optional Client
     */
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    /**
     * Get all clients with pagination
     * @param pageable pagination information
     * @return Page of Clients
     */
    @Transactional(readOnly = true)
    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    /**
     * Search clients by name
     * @param query search query
     * @return List of Clients
     */
    @Transactional(readOnly = true)
    public List<Client> searchClients(String query) {
        return clientRepository.findByNameContainingIgnoreCase(query);
    }

    /**
     * Update client
     * @param id Client ID
     * @param clientRequest updated Client data
     * @return updated Client
     */
    public Optional<Client> updateClient(Long id, ClientRequest clientRequest) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            
            // Update fields
            client.setName(clientRequest.getName());
            client.setAddress(clientRequest.getAddress());
            client.setContactNumber(clientRequest.getContactNumber());

            return Optional.of(clientRepository.save(client));
        }
        return Optional.empty();
    }

    /**
     * Delete client
     * @param id Client ID
     * @return true if deleted, false if not found
     */
    public boolean deleteClient(Long id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
