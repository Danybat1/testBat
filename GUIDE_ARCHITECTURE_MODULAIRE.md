# Guide d'Architecture Modulaire FreightOps

## 📋 Vue d'ensemble

Ce guide détaille la nouvelle architecture modulaire de FreightOps, comprenant :
- **Frontend Angular** avec modules lazy-loaded et sidebar de navigation
- **Backend Spring Boot** restructuré par domaines métier
- **Endpoints REST API** disponibles pour chaque module

---

## 🎯 Frontend Angular - Navigation et Modules

### Structure Modulaire

```
src/app/modules/
├── dashboard/                 # Tableau de bord principal
├── fret/                     # Module parent Fret
│   ├── create-lta/          # Création LTA
│   ├── list-lta/            # Liste des LTAs
│   ├── create-manifest/     # Création manifeste
│   ├── report-manifest/     # Rapports manifeste
│   ├── tracking/            # Suivi des colis
│   └── depot-management/    # Gestion dépôt
├── billetrerie/             # Gestion billets transport
├── facturation/             # Module facturation
└── finances/                # Module parent Finances
    ├── tresorerie/          # Trésorerie
    └── comptabilite/        # Comptabilité
```

### Navigation par la Sidebar

#### 📊 **Dashboard**
- **URL:** `/dashboard`
- **Description:** Tableau de bord principal avec statistiques globales
- **Fonctionnalités:** Métriques LTA, revenus, activités récentes

#### 🚛 **Fret** (Menu déroulant)
- **Créer LTA:** `/fret/create-lta`
- **Liste LTA:** `/fret/list-lta`
- **Créer Manifeste:** `/fret/create-manifest`
- **Rapports Manifeste:** `/fret/report-manifest`
- **Suivi:** `/fret/tracking`
- **Gestion Dépôt:** `/fret/depot-management`

#### 🎫 **Billetrerie**
- **URL:** `/billetrerie`
- **Description:** Gestion des billets de transport de passagers
- **Fonctionnalités:** Création billets, statistiques, routes disponibles

#### 💰 **Finances** (Menu déroulant)
- **Trésorerie:** `/finances/tresorerie`
- **Comptabilité:** `/finances/comptabilite`

#### 🧾 **Facturation**
- **URL:** `/facturation`
- **Description:** Gestion de la facturation client

---

## 🔧 Backend Spring Boot - Architecture par Domaines

### Structure des Packages

```
com.freightops/
├── dashboard/               # Domaine Dashboard
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── (pas de repository - utilise les autres domaines)
├── fret/                   # Domaine Fret
│   ├── lta/               # Sous-domaine LTA (existant)
│   └── manifeste/         # Sous-domaine Manifeste
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       └── dto/
├── billetrerie/           # Domaine Billetrerie
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
├── facturation/           # Domaine Facturation (existant)
└── finances/              # Domaine Finances
    ├── tresorerie/        # Utilise treasury existant
    └── comptabilite/      # Utilise accounting existant
```

---

## 🌐 Endpoints REST API

### 📊 Dashboard API

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/dashboard/stats` | Statistiques globales du dashboard |
| `GET` | `/api/dashboard/recent-activities` | Activités récentes (placeholder) |
| `GET` | `/api/dashboard/revenue-chart?days=30` | Graphique des revenus (placeholder) |

**Exemple de réponse `/stats`:**
```json
{
  "totalLTAs": 0,
  "activeLTAs": 0,
  "inTransitLTAs": 0,
  "deliveredLTAs": 0,
  "totalRevenue": 0.0,
  "monthlyRevenue": 0.0
}
```

### 🚛 Fret - Manifeste API

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/fret/manifests` | Créer un nouveau manifeste |
| `GET` | `/api/fret/manifests` | Lister tous les manifestes |
| `GET` | `/api/fret/manifests/{id}` | Récupérer un manifeste par ID |
| `GET` | `/api/fret/manifests/reports` | Rapports de manifestes (placeholder) |

**Exemple de requête POST `/manifests`:**
```json
{
  "manifestNumber": "MAN-2024-001",
  "expeditionDate": "2024-09-05",
  "destination": "Douala",
  "driver": "Jean Dupont",
  "vehicle": "Camion-001",
  "ltaIds": [1, 2, 3]
}
```

**Exemple de réponse:**
```json
{
  "id": 1,
  "manifestNumber": "MAN-2024-001",
  "expeditionDate": "2024-09-05",
  "destination": "Douala",
  "driver": "Jean Dupont",
  "vehicle": "Camion-001",
  "status": "CREATED",
  "ltaCount": 3,
  "totalWeight": 150.5,
  "createdAt": "2024-09-05T15:30:00",
  "message": "Manifest created successfully"
}
```

### 🎫 Billetrerie API

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/billetrerie/tickets` | Créer un nouveau billet |
| `GET` | `/api/billetrerie/tickets` | Lister tous les billets |
| `GET` | `/api/billetrerie/tickets/{id}` | Récupérer un billet par ID |
| `GET` | `/api/billetrerie/stats` | Statistiques billetrerie (placeholder) |
| `GET` | `/api/billetrerie/routes` | Routes disponibles (placeholder) |

**Exemple de requête POST `/tickets`:**
```json
{
  "passengerName": "Marie Kouam",
  "passengerPhone": "+237123456789",
  "passengerEmail": "marie@example.com",
  "origin": "Yaoundé",
  "destination": "Douala",
  "travelDate": "2024-09-10",
  "departureTime": "08:00",
  "seatNumber": "A12",
  "price": 5000.0,
  "paymentMethod": "CASH"
}
```

### 🧾 Facturation API (Existant)
- Utilise les endpoints existants du module facturation
- Pas de modification nécessaire

### 💰 Finances API (Existant)
- **Trésorerie:** Utilise les endpoints du module `treasury` existant
- **Comptabilité:** Utilise les endpoints du module `accounting` existant

---

## 🔄 Migration et Compatibilité

### Endpoints Préservés
- ✅ Tous les endpoints LTA existants (`/api/lta/*`)
- ✅ Tous les endpoints facturation existants
- ✅ Tous les endpoints treasury/accounting existants

### Nouveaux Endpoints
- 🆕 Dashboard: `/api/dashboard/*`
- 🆕 Manifeste: `/api/fret/manifests/*`
- 🆕 Billetrerie: `/api/billetrerie/*`

### Routes Frontend Préservées
- ✅ `/lta/*` - Toutes les routes LTA existantes
- ✅ `/treasury/*` - Routes trésorerie
- ✅ `/accounting/*` - Routes comptabilité

---

## 🚀 Démarrage et Tests

### Frontend Angular
```bash
cd frontend
npm install
ng serve
```
- **URL:** http://localhost:4200
- **Navigation:** Utiliser la sidebar pour accéder aux modules

### Backend Spring Boot
```bash
cd backend
./mvnw spring-boot:run
```
- **URL:** http://localhost:8080
- **API Documentation:** Endpoints listés ci-dessus

### Tests de Navigation
1. **Dashboard:** Vérifier l'affichage des statistiques
2. **Fret → Créer LTA:** Tester le formulaire accordion existant
3. **Fret → Liste LTA:** Vérifier la liste des LTAs
4. **Fret → Créer Manifeste:** Tester le formulaire placeholder
5. **Billetrerie:** Vérifier l'interface placeholder
6. **Finances → Trésorerie:** Accès au module treasury
7. **Finances → Comptabilité:** Accès au module accounting

---

## 📝 Notes de Développement

### Modules Placeholder
Les modules suivants sont implémentés avec des interfaces placeholder :
- **Créer Manifeste** - Formulaire de base prêt pour développement
- **Rapports Manifeste** - Interface à développer
- **Suivi** - Interface à développer
- **Gestion Dépôt** - Interface à développer
- **Billetrerie** - Interface de base avec statistiques

### Prochaines Étapes
1. Implémenter la logique métier dans les services placeholder
2. Connecter les endpoints backend aux vrais services
3. Ajouter la validation des formulaires
4. Implémenter les rapports et statistiques
5. Ajouter les tests unitaires et d'intégration

### Architecture Benefits
- ✅ **Modularité:** Chaque domaine est isolé et maintenable
- ✅ **Lazy Loading:** Performance optimisée avec chargement à la demande
- ✅ **Évolutivité:** Facile d'ajouter de nouveaux modules
- ✅ **Réutilisabilité:** Modules existants préservés et réutilisés
- ✅ **Navigation Intuitive:** Sidebar organisée par domaines métier
