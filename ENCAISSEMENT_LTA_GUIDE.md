# Guide d'utilisation - Encaissement LTA

## Vue d'ensemble

La fonctionnalité d'encaissement LTA permet d'encaisser directement les LTA (Lettres de Transport Aérien) depuis les opérations de caisse avec comptabilité double écriture automatique.

## Prérequis

- LTA avec statut `CONFIRMED`, `IN_TRANSIT` ou `DELIVERED`
- Mode de paiement `CASH` ou `PORT_DU`
- Coût calculé (`calculatedCost`) défini et supérieur à 0
- Montant non entièrement soldé

## Utilisation

### 1. Accès à la fonctionnalité

1. Aller dans **Trésorerie** → **Opérations de Caisse**
2. Sélectionner **Type d'opération** : `Encaissement LTA`
3. La section de sélection LTA apparaît automatiquement

### 2. Sélection de la LTA

1. **Liste des LTA disponibles** : Seules les LTA non entièrement soldées sont affichées
2. **Format d'affichage** : `LTA-XXXXX - Origine → Destination (Montant USD)`
3. **Sélection** : Choisir la LTA dans la liste déroulante

### 3. Informations automatiques

Après sélection de la LTA, les champs suivants sont pré-remplis :

- **Montant** : Montant restant à payer
- **Client/Fournisseur** : Nom du client ou "Client LTA"
- **Description** : `Encaissement LTA [Numéro] - [Trajet]`
- **Référence** : `LTA-[Numéro]`
- **Nature d'opération** : "Encaissement LTA"
- **Méthode de paiement** : "Espèces" (par défaut)

### 4. Détails de la LTA

Un panneau d'information affiche :
- **Numéro LTA** et **trajet**
- **Mode de paiement** (CASH/PORT_DU)
- **Coût total** et **montant restant**
- **Client** associé

### 5. Finalisation

1. **Vérifier les informations** pré-remplies
2. **Modifier si nécessaire** : montant (partiel autorisé), méthode de paiement
3. **Ajouter des observations** (optionnel)
4. **Cliquer sur "Enregistrer"**

## Méthodes de paiement supportées

- **Espèces** : Encaissement en caisse
- **Port dû** : Paiement différé client

## Comptabilité automatique

### Écritures générées

**Pour paiement en espèces :**
- **Débit** : 5111 - Caisse espèces
- **Crédit** : 7061 - Ventes de services transport

**Pour port dû :**
- **Débit** : 4111 - Clients - Port dû  
- **Crédit** : 7061 - Ventes de services transport

### Référence comptable

Format : `LTA-PAY-[timestamp]`
Exemple : `LTA-PAY-1693648200000`

## Validation et contrôles

### Contrôles automatiques

- **Montant maximum** : Ne peut pas dépasser le montant restant dû
- **LTA valide** : Vérification de l'existence et du statut
- **Caisse active** : Validation pour paiements espèces

### Messages d'erreur

- `"Le montant à payer dépasse le montant restant dû"`
- `"LTA non trouvée avec l'ID: [ID]"`
- `"Caisse non trouvée avec l'ID: [ID]"`

## Traçabilité

### Historique des paiements

Chaque paiement est enregistré avec :
- **Date et heure** de création
- **Montant** et **méthode de paiement**
- **Référence comptable** unique
- **Utilisateur** ayant effectué l'opération
- **Notes** éventuelles

### Consultation

Les paiements peuvent être consultés via :
- **API** : `/api/lta-payments/by-lta/{ltaId}`
- **Résumé** : `/api/lta-payments/summary/{ltaId}`

## Endpoints API

### LTA non soldées
```
GET /api/lta-payments/unpaid-ltas
```

### Montant restant
```
GET /api/lta-payments/remaining-amount/{ltaId}
```

### Enregistrement paiement
```
POST /api/lta-payments/record-payment
{
  "ltaId": 123,
  "amount": 150.00,
  "paymentMethod": "ESPECES",
  "reference": "LTA-12345",
  "notes": "Paiement partiel",
  "cashBoxId": 1
}
```

### Résumé paiements
```
GET /api/lta-payments/summary/{ltaId}
```

## Cas d'usage

### Encaissement total
1. Client vient payer intégralement sa LTA
2. Sélectionner la LTA → montant total pré-rempli
3. Confirmer l'encaissement

### Encaissement partiel
1. Client effectue un acompte
2. Sélectionner la LTA → modifier le montant
3. Enregistrer → solde restant mis à jour

### Port dû
1. LTA en mode PORT_DU livrée
2. Sélectionner méthode "Port dû"
3. Enregistrer la créance client

## Dépannage

### LTA n'apparaît pas dans la liste

**Vérifier :**
- Statut LTA : doit être CONFIRMED/IN_TRANSIT/DELIVERED
- Mode paiement : doit être CASH ou PORT_DU  
- Coût calculé : doit être défini et > 0
- Solde : ne doit pas être entièrement payé

### Erreur lors de l'enregistrement

**Actions :**
1. Vérifier la connexion backend
2. Contrôler les logs serveur
3. Valider les données saisies
4. Redémarrer l'application si nécessaire

## Support technique

Pour tout problème technique :
1. Consulter les logs backend
2. Vérifier la base de données (table `lta_payments`)
3. Contrôler les références comptables
4. Contacter l'équipe de développement
