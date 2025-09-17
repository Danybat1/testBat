package com.freightops.config;

import com.freightops.entity.Client;
import com.freightops.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initializes the database with sample Congolese clients
 */
@Component
@Order(3)
public class ClientInitializer implements CommandLineRunner {

        @Autowired
        private ClientRepository clientRepository;

        @Override
        public void run(String... args) throws Exception {
                initializeDRCClients();
        }

        private void initializeDRCClients() {
                // Check if clients already exist to avoid duplicates
                if (clientRepository.count() > 0) {
                        return;
                }

                // Create clients with proper constructor and set email separately
                createClient("SONAS - Société Nationale d'Assurance",
                                "Avenue Kasa-Vubu, Kinshasa, RD Congo",
                                "+243 81 234 5678",
                                "contact@sonas.cd");

                createClient("Banque Centrale du Congo",
                                "Boulevard du 30 Juin, Kinshasa, RD Congo",
                                "+243 81 345 6789",
                                "info@bcc.cd");

                createClient("Gécamines SARL",
                                "Avenue de la Libération, Kinshasa, RD Congo",
                                "+243 81 456 7890",
                                "gecamines@gecamines.cd");

                createClient("Société Minière de Bakwanga (MIBA)",
                                "Avenue des Martyrs, Kinshasa, RD Congo",
                                "+243 81 567 8901",
                                "contact@miba.cd");

                // Entreprises de Lubumbashi
                createClient("Tenke Fungurume Mining",
                                "Avenue Mobutu, Lubumbashi, RD Congo",
                                "+243 82 234 5678",
                                "info@tfm.cd");

                createClient("Kamoto Copper Company",
                                "Boulevard Kamanyola, Lubumbashi, RD Congo",
                                "+243 82 345 6789",
                                "contact@kcc.cd");

                createClient("Mutanda Mining SARL",
                                "Avenue de l'Université, Lubumbashi, RD Congo",
                                "+243 82 456 7890",
                                "info@mutanda.cd");

                // Entreprises de Goma
                createClient("Banque Commerciale du Congo - Goma",
                                "Avenue de l'Indépendance, Goma, RD Congo",
                                "+243 83 234 5678",
                                "goma@bcdc.cd");

                createClient("Société Minière du Kivu",
                                "Route de Sake, Goma, RD Congo",
                                "+243 83 345 6789",
                                "contact@smk.cd");

                // Entreprises de Kisangani
                createClient("Société de Transport Fluvial",
                                "Port de Kisangani, RD Congo",
                                "+243 84 234 5678",
                                "transport@stf.cd");

                createClient("Compagnie Forestière du Congo",
                                "Avenue Lumumba, Kisangani, RD Congo",
                                "+243 84 345 6789",
                                "info@cfc.cd");

                // Entreprises de Mbuji-Mayi
                createClient("Diamond Trading Company",
                                "Avenue des Diamants, Mbuji-Mayi, RD Congo",
                                "+243 85 234 5678",
                                "contact@dtc.cd");

                createClient("Société Minière de Kasaï",
                                "Boulevard Tshombe, Mbuji-Mayi, RD Congo",
                                "+243 85 345 6789",
                                "info@smk-kasai.cd");

                // Entreprises de Bukavu
                createClient("Société de Café du Kivu",
                                "Avenue de la Paix, Bukavu, RD Congo",
                                "+243 86 234 5678",
                                "cafe@kivu.cd");

                createClient("Banque de Crédit Agricole - Bukavu",
                                "Place de l'Indépendance, Bukavu, RD Congo",
                                "+243 86 345 6789",
                                "bukavu@bca.cd");

                // Entreprises de Kananga
                createClient("Compagnie Sucrière du Kasaï",
                                "Zone Industrielle, Kananga, RD Congo",
                                "+243 87 234 5678",
                                "sucre@csk.cd");

                // Entreprises de Matadi
                createClient("Port Autonome de Matadi",
                                "Boulevard Maritime, Matadi, RD Congo",
                                "+243 88 234 5678",
                                "port@matadi.cd");

                createClient("Société de Transport Maritime",
                                "Quai Principal, Matadi, RD Congo",
                                "+243 88 345 6789",
                                "maritime@stm.cd");

                // Entreprises de Kolwezi
                createClient("Société d'Exploitation Minière",
                                "Avenue Industrielle, Kolwezi, RD Congo",
                                "+243 89 234 5678",
                                "mining@sem.cd");

                // Entreprises de Mbandaka
                createClient("Société Forestière de l'Équateur",
                                "Route Fluviale, Mbandaka, RD Congo",
                                "+243 90 234 5678",
                                "foret@sfe.cd");

        }

        private void createClient(String name, String address, String contactNumber, String email) {
                Client client = new Client(name, address, contactNumber);
                client.setEmail(email);
                clientRepository.save(client);
        }
}
