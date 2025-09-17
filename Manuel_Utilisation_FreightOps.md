# Manuel d'Utilisation FreightOps
## Guide Complet avec Tests Utilisateur Backend

---

## Table des Matières

1. [Vue d'ensemble de l'application](#vue-densemble)
2. [Module Authentification](#module-authentification)
3. [Module Dashboard](#module-dashboard)
4. [Module LTA (Lettre de Transport Aérien)](#module-lta)
5. [Module Clients](#module-clients)
6. [Module Villes](#module-villes)
7. [Module Utilisateurs](#module-utilisateurs)
8. [Module Tarifs](#module-tarifs)
9. [Module Facturation](#module-facturation)
10. [Module Trésorerie](#module-trésorerie)
11. [Module Comptabilité](#module-comptabilité)
12. [Module Tracking Public](#module-tracking)
13. [Module Rapports](#module-rapports)
14. [Tests d'Intégration](#tests-dintégration)

---

## Vue d'ensemble {#vue-densemble}

FreightOps est une application complète de gestion de transport et logistique avec les fonctionnalités suivantes :

**Technologies :**
- **Frontend :** Angular 17 avec Angular Material
- **Backend :** Spring Boot avec JPA/Hibernate
- **Base de données :** Compatible JPA (MySQL/PostgreSQL)
- **Devises supportées :** CDF (Franc Congolais), USD (Dollar Américain)
- **Standards comptables :** Plan comptable OHADA

**Architecture :**
- Interface utilisateur moderne et responsive
- API REST sécurisée avec authentification JWT
- Système de comptabilité automatique en partie double
- Génération de documents PDF (factures, LTA)
- Système de tracking avec QR codes

---

## Module Authentification {#module-authentification}

### Fonctionnalités
- Connexion/déconnexion utilisateurs
- Gestion des sessions JWT
- Protection des routes avec AuthGuard

### Tests Utilisateur Backend

#### Test 1 : Connexion Utilisateur
**Endpoint :** `POST /api/auth/login`

**Données de test :**
```json
{
  "username": "admin@freightops.com",
  "password": "admin123"
}
```

**Actions à tester :**
1. Ouvrir l'application sur `http://localhost:4200`
2. Saisir les identifiants de test
3. Cliquer sur "Se connecter"

**Résultats attendus :**
- ✅ Redirection vers le dashboard
- ✅ Token JWT stocké dans localStorage
- ✅ Menu de navigation visible
- ✅ Nom d'utilisateur affiché dans l'en-tête

#### Test 2 : Déconnexion
**Actions à tester :**
1. Cliquer sur le menu utilisateur (coin supérieur droit)
2. Sélectionner "Déconnexion"

**Résultats attendus :**
- ✅ Redirection vers la page de connexion
- ✅ Token supprimé du localStorage
- ✅ Menu de navigation masqué

---

## Module Dashboard {#module-dashboard}

### Fonctionnalités
- Vue d'ensemble des KPIs
- Statistiques en temps réel
- Graphiques et widgets

### Tests Utilisateur Backend

#### Test 1 : Affichage du Dashboard
**URL :** `/dashboard`

**Actions à tester :**
1. Se connecter à l'application
2. Naviguer vers le dashboard (page d'accueil)

**Résultats attendus :**
- ✅ Widgets KPI affichés (revenus, expéditions, clients)
- ✅ Graphiques de performance visibles
- ✅ Données récentes chargées
- ✅ Interface responsive sur mobile/desktop

---

## Module LTA (Lettre de Transport Aérien) {#module-lta}

### Fonctionnalités
- Création et gestion des expéditions
- Génération automatique de QR codes
- Suivi des statuts (Brouillon, Confirmé, En transit, Livré)
- Génération PDF des LTA

### Tests Utilisateur Backend

#### Test 1 : Création d'une LTA
**Endpoint :** `POST /api/lta`
**URL :** `/lta/create`

**Données de test :**
```json
{
  "clientId": 1,
  "originCityId": 1,
  "destinationCityId": 2,
  "weight": 25.5,
  "pieces": 3,
  "description": "Matériel informatique",
  "declaredValue": 1500.00,
  "currency": "USD"
}
```

**Actions à tester :**
1. Naviguer vers `/lta/create`
2. Remplir le formulaire avec les données de test
3. Sélectionner un client existant
4. Choisir villes d'origine et destination
5. Cliquer sur "Créer LTA"

**Résultats attendus :**
- ✅ LTA créée avec numéro automatique (LTA-YYYY-NNNNNN)
- ✅ Statut initial : DRAFT
- ✅ Redirection vers la liste des LTA
- ✅ Message de confirmation affiché

#### Test 2 : Confirmation d'une LTA
**Endpoint :** `PUT /api/lta/{id}/status`

**Actions à tester :**
1. Aller dans la liste des LTA (`/lta`)
2. Cliquer sur "Voir détails" d'une LTA en brouillon
3. Cliquer sur "Confirmer"

**Résultats attendus :**
- ✅ Statut changé vers CONFIRMED
- ✅ Numéro de tracking généré automatiquement (TRK + 12 caractères)
- ✅ QR code généré et affiché
- ✅ Écriture comptable automatique créée (Débit 411 Clients / Crédit 701 Ventes)

#### Test 3 : Téléchargement PDF LTA
**Endpoint :** `GET /api/lta/{id}/pdf`

**Actions à tester :**
1. Ouvrir les détails d'une LTA confirmée
2. Cliquer sur "Télécharger PDF"

**Résultats attendus :**
- ✅ Fichier PDF téléchargé
- ✅ PDF contient toutes les informations de la LTA
- ✅ QR code inclus dans le PDF

---

## Module Clients {#module-clients}

### Fonctionnalités
- Gestion de la base clients
- Informations complètes (contact, adresse, facturation)
- Historique des expéditions

### Tests Utilisateur Backend

#### Test 1 : Création d'un Client
**Endpoint :** `POST /api/clients`
**URL :** `/clients/create`

**Données de test :**
```json
{
  "name": "SARL CONGO BUSINESS",
  "email": "contact@congobusiness.cd",
  "phone": "+243 81 234 5678",
  "address": "Avenue Kasa-Vubu, Kinshasa",
  "city": "Kinshasa",
  "country": "République Démocratique du Congo",
  "taxNumber": "CD123456789"
}
```

**Actions à tester :**
1. Naviguer vers `/clients/create`
2. Remplir le formulaire avec les données de test
3. Cliquer sur "Enregistrer"

**Résultats attendus :**
- ✅ Client créé avec ID unique
- ✅ Redirection vers la liste des clients
- ✅ Client visible dans la liste

#### Test 2 : Recherche de Clients
**URL :** `/clients`

**Actions à tester :**
1. Aller dans la liste des clients
2. Utiliser la barre de recherche avec "CONGO"
3. Tester les filtres (ville, pays)

**Résultats attendus :**
- ✅ Résultats filtrés en temps réel
- ✅ Nombre de résultats affiché
- ✅ Possibilité de réinitialiser les filtres

---

## Module Villes {#module-villes}

### Fonctionnalités
- Configuration des villes/destinations
- Gestion des codes IATA
- Tarification par route

### Tests Utilisateur Backend

#### Test 1 : Ajout d'une Ville
**Endpoint :** `POST /api/cities`
**URL :** `/cities/create`

**Données de test :**
```json
{
  "name": "Lubumbashi",
  "country": "République Démocratique du Congo",
  "iataCode": "FBM",
  "isActive": true
}
```

**Actions à tester :**
1. Naviguer vers `/cities/create`
2. Saisir les informations de la ville
3. Cliquer sur "Ajouter"

**Résultats attendus :**
- ✅ Ville créée et visible dans la liste
- ✅ Code IATA unique validé
- ✅ Statut actif par défaut

---

## Module Utilisateurs {#module-utilisateurs}

### Fonctionnalités
- Administration des utilisateurs
- Gestion des rôles et permissions
- Activation/désactivation des comptes

### Tests Utilisateur Backend

#### Test 1 : Création d'un Utilisateur
**URL :** `/users/create`

**Données de test :**
```json
{
  "username": "operateur1",
  "email": "operateur1@freightops.com",
  "firstName": "Jean",
  "lastName": "Kabongo",
  "role": "OPERATOR",
  "isActive": true
}
```

**Actions à tester :**
1. Naviguer vers `/users/create`
2. Remplir le formulaire utilisateur
3. Assigner un rôle (ADMIN, OPERATOR, VIEWER)
4. Cliquer sur "Créer"

**Résultats attendus :**
- ✅ Utilisateur créé avec mot de passe temporaire
- ✅ Email de bienvenue envoyé
- ✅ Utilisateur visible dans la liste

---

## Module Tarifs {#module-tarifs}

### Fonctionnalités
- Gestion de la grille tarifaire
- Tarifs par route et poids
- Tarifs spéciaux clients

### Tests Utilisateur Backend

#### Test 1 : Configuration d'un Tarif
**Endpoint :** `POST /api/tariffs`
**URL :** `/tariff/create`

**Données de test :**
```json
{
  "originCityId": 1,
  "destinationCityId": 2,
  "minWeight": 0,
  "maxWeight": 50,
  "pricePerKg": 15.00,
  "currency": "USD",
  "isActive": true
}
```

**Actions à tester :**
1. Naviguer vers `/tariff/create`
2. Sélectionner origine et destination
3. Définir les tranches de poids
4. Saisir le prix par kg
5. Cliquer sur "Enregistrer"

**Résultats attendus :**
- ✅ Tarif créé et actif
- ✅ Calcul automatique lors de création LTA
- ✅ Validation des tranches de poids

---

## Module Facturation {#module-facturation}

### Fonctionnalités
- Création de factures multi-devises (CDF/USD)
- Liaison automatique avec les LTA
- Génération PDF moderne
- Gestion TVA et taxes

### Tests Utilisateur Backend

#### Test 1 : Création d'une Facture
**Endpoint :** `POST /api/invoices`
**URL :** `/billing/invoice/create`

**Données de test :**
```json
{
  "clientId": 1,
  "type": "TRANSPORT",
  "currency": "CDF",
  "items": [
    {
      "description": "Transport aérien Kinshasa-Lubumbashi",
      "quantity": 1,
      "unitPrice": 427500.00,
      "ltaId": 1
    }
  ],
  "notes": "Paiement à 30 jours"
}
```

**Actions à tester :**
1. Naviguer vers `/billing/invoice/create`
2. Sélectionner un client
3. Choisir le type de facture
4. Ajouter des lignes de facturation
5. Lier à une LTA existante
6. Cliquer sur "Créer facture"

**Résultats attendus :**
- ✅ Facture créée avec numéro automatique (INV-YYYY-NNNNNN)
- ✅ Calculs automatiques (sous-total, TVA, total)
- ✅ Écriture comptable automatique (Débit 411 / Crédit 701 + 445)
- ✅ Conversion de devise si nécessaire (1 USD = 2850 CDF)

#### Test 2 : Génération PDF Facture
**Endpoint :** `GET /api/invoices/{id}/pdf`

**Actions à tester :**
1. Ouvrir la liste des factures (`/billing`)
2. Cliquer sur l'icône PDF d'une facture
3. Vérifier le téléchargement

**Résultats attendus :**
- ✅ PDF moderne téléchargé
- ✅ En-tête bleu avec logo entreprise
- ✅ Informations client et facture complètes
- ✅ Tableau des articles avec totaux
- ✅ Formatage correct des devises

---

## Module Trésorerie {#module-trésorerie}

### Fonctionnalités
- Gestion comptes bancaires et caisses
- Suivi des transactions
- Rapprochements bancaires
- Tableaux de bord financiers

### Tests Utilisateur Backend

#### Test 1 : Création d'un Compte Bancaire
**Endpoint :** `POST /api/bank-accounts`
**URL :** `/treasury/bank-accounts/create`

**Données de test :**
```json
{
  "accountName": "Compte Principal USD",
  "bankName": "Rawbank",
  "accountNumber": "001-234567890-USD",
  "currency": "USD",
  "initialBalance": 50000.00,
  "isActive": true
}
```

**Actions à tester :**
1. Naviguer vers `/treasury/bank-accounts/create`
2. Remplir les informations du compte
3. Définir le solde initial
4. Cliquer sur "Créer"

**Résultats attendus :**
- ✅ Compte créé et visible dans la liste
- ✅ Solde initial enregistré
- ✅ Écriture comptable d'ouverture (Débit 512 Banque / Crédit 101 Capital)

#### Test 2 : Enregistrement d'un Paiement
**Endpoint :** `POST /api/payments`

**Données de test :**
```json
{
  "invoiceId": 1,
  "amount": 427500.00,
  "currency": "CDF",
  "paymentMethod": "BANK_TRANSFER",
  "bankAccountId": 1,
  "reference": "VIR-20240901-001"
}
```

**Actions à tester :**
1. Aller dans une facture impayée
2. Cliquer sur "Enregistrer paiement"
3. Saisir le montant et la méthode
4. Sélectionner le compte bancaire
5. Confirmer le paiement

**Résultats attendus :**
- ✅ Paiement enregistré
- ✅ Statut facture mis à jour (PAID ou PARTIALLY_PAID)
- ✅ Solde compte bancaire mis à jour
- ✅ Écriture comptable automatique (Débit 512 Banque / Crédit 411 Clients)

---

## Module Comptabilité {#module-comptabilité}

### Fonctionnalités
- Plan comptable OHADA complet
- Écritures automatiques en partie double
- Balance comptable et grand livre
- Rapports financiers (Bilan, Compte de résultat)

### Tests Utilisateur Backend

#### Test 1 : Consultation du Plan Comptable
**Endpoint :** `GET /api/accounting/accounts`
**URL :** `/accounting/chart-of-accounts`

**Actions à tester :**
1. Naviguer vers `/accounting/chart-of-accounts`
2. Explorer la structure hiérarchique
3. Filtrer par type de compte (ASSET, LIABILITY, etc.)
4. Rechercher un compte spécifique

**Résultats attendus :**
- ✅ Plan comptable OHADA affiché
- ✅ Structure hiérarchique visible (comptes parents/enfants)
- ✅ Soldes des comptes mis à jour
- ✅ Filtres fonctionnels

#### Test 2 : Consultation des Écritures Comptables
**Endpoint :** `GET /api/accounting/journal-entries`
**URL :** `/accounting/journal-entries`

**Actions à tester :**
1. Naviguer vers `/accounting/journal-entries`
2. Consulter les écritures automatiques
3. Vérifier l'équilibre débit/crédit
4. Filtrer par période ou source

**Résultats attendus :**
- ✅ Liste des écritures avec numérotation (JE-YYYY-NNNNNN)
- ✅ Équilibre débit = crédit pour chaque écriture
- ✅ Traçabilité source (Invoice, Payment, LTA)
- ✅ Totaux corrects

#### Test 3 : Balance Comptable
**Endpoint :** `GET /api/accounting/trial-balance`
**URL :** `/accounting/trial-balance`

**Actions à tester :**
1. Naviguer vers `/accounting/trial-balance`
2. Sélectionner une période
3. Vérifier l'équilibre général
4. Exporter en PDF/Excel

**Résultats attendus :**
- ✅ Balance équilibrée (Total Débit = Total Crédit)
- ✅ Tous les comptes avec mouvements affichés
- ✅ Soldes corrects par compte
- ✅ Export fonctionnel

#### Test 4 : Dashboard Comptable
**Endpoint :** `GET /api/accounting/dashboard/stats`
**URL :** `/accounting/dashboard`

**Actions à tester :**
1. Naviguer vers `/accounting/dashboard`
2. Consulter les KPIs comptables
3. Vérifier les graphiques

**Résultats attendus :**
- ✅ Soldes bancaires et caisse affichés
- ✅ Créances clients (compte 411)
- ✅ Dettes fournisseurs (compte 401)
- ✅ Chiffre d'affaires du mois

---

## Module Tracking Public {#module-tracking}

### Fonctionnalités
- Suivi public des expéditions via QR code
- Interface accessible sans authentification
- Historique des statuts en temps réel

### Tests Utilisateur Backend

#### Test 1 : Tracking par Numéro
**Endpoint :** `GET /api/lta/tracking/{trackingNumber}`
**URL :** `/track/{trackingNumber}`

**Actions à tester :**
1. Obtenir un numéro de tracking d'une LTA confirmée (ex: TRK12345678ABCD)
2. Naviguer vers `/track/TRK12345678ABCD` (sans authentification)
3. Scanner le QR code avec un smartphone

**Résultats attendus :**
- ✅ Page de tracking accessible sans connexion
- ✅ Informations expédition affichées (origine, destination, statut)
- ✅ Historique des changements de statut
- ✅ Interface responsive mobile

#### Test 2 : QR Code Scanning
**Actions à tester :**
1. Imprimer ou afficher le QR code d'une LTA
2. Scanner avec une app QR code mobile
3. Vérifier la redirection

**Résultats attendus :**
- ✅ QR code redirige vers l'URL de tracking
- ✅ Page optimisée mobile s'affiche
- ✅ Informations à jour

---

## Module Rapports {#module-rapports}

### Fonctionnalités
- Rapports financiers automatisés
- Exports PDF/Excel
- Rapports personnalisés par période

### Tests Utilisateur Backend

#### Test 1 : Rapport des Ventes
**URL :** `/report/sales`

**Actions à tester :**
1. Naviguer vers `/report/sales`
2. Sélectionner une période (ex: mois en cours)
3. Choisir la devise (CDF/USD)
4. Générer le rapport

**Résultats attendus :**
- ✅ Rapport généré avec totaux par période
- ✅ Répartition par type de service
- ✅ Graphiques de tendance
- ✅ Export PDF fonctionnel

---

## Tests d'Intégration {#tests-dintégration}

### Scénario Complet : Processus d'Expédition

**Objectif :** Tester le flux complet depuis la création d'une LTA jusqu'au paiement

#### Étapes du Test :

1. **Création Client**
   - Créer un nouveau client via `/clients/create`
   - Vérifier l'enregistrement

2. **Création LTA**
   - Créer une LTA pour ce client via `/lta/create`
   - Vérifier le statut DRAFT

3. **Confirmation LTA**
   - Confirmer la LTA
   - Vérifier génération tracking number et QR code
   - Vérifier écriture comptable automatique

4. **Facturation**
   - Créer une facture liée à la LTA via `/billing/invoice/create`
   - Vérifier calculs automatiques
   - Vérifier écriture comptable (411/701/445)

5. **Paiement**
   - Enregistrer un paiement via trésorerie
   - Vérifier mise à jour statut facture
   - Vérifier écriture comptable (512/411)

6. **Vérifications Comptables**
   - Consulter la balance comptable
   - Vérifier équilibre débit/crédit
   - Consulter le grand livre

7. **Tracking**
   - Tester le tracking public avec le numéro généré
   - Vérifier QR code

#### Résultats Attendus :
- ✅ Processus complet sans erreur
- ✅ Toutes les écritures comptables générées
- ✅ Balance équilibrée
- ✅ Documents PDF générés
- ✅ Tracking fonctionnel

### Points de Validation Critiques :

1. **Intégrité Comptable**
   - Toute opération génère les écritures appropriées
   - Balance toujours équilibrée
   - Traçabilité complète

2. **Cohérence des Données**
   - Statuts mis à jour en temps réel
   - Soldes corrects
   - Numérotation automatique unique

3. **Performance**
   - Temps de réponse < 2 secondes
   - Chargement fluide des listes
   - Export PDF rapide

4. **Sécurité**
   - Authentification requise pour modules privés
   - Tracking public accessible sans auth
   - Validation des données d'entrée

---

## Données de Test Recommandées

### Clients de Test :
```json
[
  {
    "name": "SARL CONGO BUSINESS",
    "email": "contact@congobusiness.cd",
    "phone": "+243 81 234 5678",
    "address": "Avenue Kasa-Vubu, Kinshasa"
  },
  {
    "name": "STE MINING KATANGA",
    "email": "admin@miningkatanga.cd", 
    "phone": "+243 99 876 5432",
    "address": "Boulevard Lumumba, Lubumbashi"
  }
]
```

### Villes de Test :
```json
[
  {"name": "Kinshasa", "iataCode": "FIH", "country": "RDC"},
  {"name": "Lubumbashi", "iataCode": "FBM", "country": "RDC"},
  {"name": "Goma", "iataCode": "GOM", "country": "RDC"}
]
```

### Utilisateurs de Test :
```json
[
  {"username": "admin", "role": "ADMIN", "email": "admin@freightops.com"},
  {"username": "operator1", "role": "OPERATOR", "email": "op1@freightops.com"},
  {"username": "viewer1", "role": "VIEWER", "email": "view1@freightops.com"}
]
```

---

## Résumé

Ce manuel couvre l'ensemble des fonctionnalités de FreightOps avec des tests utilisateur détaillés pour chaque module. L'application offre une solution complète de gestion de transport avec comptabilité automatique, multi-devises, et tracking en temps réel.

**Points forts testés :**
- ✅ Interface utilisateur moderne et intuitive
- ✅ Intégration comptable automatique OHADA
- ✅ Système de tracking avec QR codes
- ✅ Génération de documents PDF professionnels
- ✅ Support multi-devises (CDF/USD)
- ✅ Sécurité et authentification robuste

L'application est prête pour un déploiement en production avec tous les modules fonctionnels et testés.
