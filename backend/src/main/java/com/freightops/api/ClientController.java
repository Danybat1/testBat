package com.freightops.api;

import com.freightops.entity.Client;
import com.freightops.dto.ClientRequest;
import com.freightops.dto.ClientResponse;
import com.freightops.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Client REST Controller
 * Handles HTTP requests for Client operations
 */
@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Create a new client
     * POST /api/client
     */
    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientRequest clientRequest) {
        try {
            Client createdClient = clientService.createClient(clientRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(createdClient));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create client: " + e.getMessage()));
        }
    }

    /**
     * Get all clients with pagination
     * GET /api/client
     */
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Client> clientPage = clientService.getAllClients(pageable);
        Page<ClientResponse> clientResponsePage = clientPage.map(this::convertToResponse);
        return ResponseEntity.ok(clientResponsePage);
    }

    /**
     * Search clients by name
     * GET /api/client/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClientResponse>> searchClients(@RequestParam String query) {
        List<Client> clients = clientService.searchClients(query);
        List<ClientResponse> clientResponses = clients.stream()
                .map(this::convertToResponse)
                .toList();
        return ResponseEntity.ok(clientResponses);
    }

    /**
     * Get client by ID
     * GET /api/client/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        Optional<Client> clientOpt = clientService.getClientById(id);
        if (clientOpt.isPresent()) {
            return ResponseEntity.ok(convertToResponse(clientOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update client
     * PUT /api/client/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest clientRequest) {
        try {
            Optional<Client> updatedClient = clientService.updateClient(id, clientRequest);
            if (updatedClient.isPresent()) {
                return ResponseEntity.ok(convertToResponse(updatedClient.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update client: " + e.getMessage()));
        }
    }

    /**
     * Delete client
     * DELETE /api/client/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            boolean deleted = clientService.deleteClient(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete client: " + e.getMessage()));
        }
    }

    /**
     * Convert Client entity to Response DTO
     */
    private ClientResponse convertToResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setName(client.getName());
        response.setAddress(client.getAddress());
        response.setContactNumber(client.getContactNumber());
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());
        return response;
    }

    // Inner class for error responses
    public static class ErrorResponse {
        private String message;
        private long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
