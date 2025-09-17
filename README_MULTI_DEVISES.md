# 💰 Système Multi-Devises FreightOps

## 📋 Vue d'ensemble

Le système multi-devises FreightOps permet la gestion complète des devises USD et CDF (Franc Congolais) avec conversion automatique, formatage cohérent et mise à jour en temps réel des taux de change.

## 🏗️ Architecture

### Backend (Spring Boot)

#### Entités
- **`Currency`** : Gestion des devises (code, nom, symbole, décimales)
- **`ExchangeRate`** : Gestion des taux de change avec historique

#### Services
- **`CurrencyService`** : Service principal avec cache et conversion
- **`CurrencyRepository`** & **`ExchangeRateRepository`** : Accès aux données

#### API REST
```
GET    /api/currencies              # Liste des devises
GET    /api/currencies/default      # Devise par défaut
GET    /api/currencies/{code}       # Devise par code
POST   /api/currencies/convert      # Conversion de montant
GET    /api/currencies/rate/{from}/{to}  # Taux de change
GET    /api/currencies/rates        # Tous les taux
PUT    /api/currencies/rates        # Mise à jour taux (Admin)
POST   /api/currencies/format       # Formatage montant
POST   /api/currencies/initialize   # Initialisation données
```

### Frontend (Angular)

#### Services
- **`CurrencyService`** : Service principal avec observables RxJS
- Mise à jour automatique toutes les 5 minutes
- Cache local et fallback hors ligne

#### Composants
- **`CurrencyDisplayComponent`** : Affichage formaté avec conversion
- **`CurrencySelectorComponent`** : Sélecteur de devise amélioré
- **`CurrencyAdminComponent`** : Interface d'administration

#### Pipe
- **`CurrencyFormatPipe`** : Formatage automatique dans les templates

## 🚀 Utilisation

### 1. Composant d'affichage de montant

```html
<!-- Affichage simple -->
<app-currency-display 
  [amount]="1000" 
  sourceCurrency="USD">
</app-currency-display>

<!-- Avec montant original -->
<app-currency-display 
  [amount]="1000" 
  sourceCurrency="USD"
  [showOriginal]="true"
  size="lg"
  color="success">
</app-currency-display>
```

### 2. Pipe de formatage

```html
<!-- Dans un template -->
{{ montant | currencyFormat:'USD':true | async }}

<!-- Dans un tableau -->
<td>{{ item.price | currencyFormat:item.currency | async }}</td>
```

### 3. Service dans un composant

```typescript
constructor(private currencyService: CurrencyService) {}

ngOnInit() {
  // Conversion
  this.currencyService.convert(1000, 'USD', 'CDF').subscribe(result => {
    console.log(result.formattedAmount); // "2 700 000 FC"
  });
  
  // Écouter les changements de devise
  this.currencyService.currentCurrency$.subscribe(currency => {
    this.updatePrices(currency);
  });
}
```

### 4. Administration des taux

```html
<!-- Composant d'administration -->
<app-currency-admin></app-currency-admin>
```

## 📊 Taux de Change par Défaut

| Paire | Taux | Description |
|-------|------|-------------|
| USD → CDF | 2700 | 1 USD = 2700 FC |
| CDF → USD | 0.000370 | 1 FC = 0.000370 USD |

## 🔧 Configuration

### Environment

```typescript
// environment.ts
export const environment = {
  currency: {
    refreshInterval: 300000, // 5 minutes
    fallbackRates: {
      'USD_CDF': 2700,
      'CDF_USD': 0.000370
    },
    defaultCurrency: 'USD',
    supportedCurrencies: ['USD', 'CDF']
  }
};
```

### Base de données

```sql
-- Création des tables
CREATE TABLE currencies (
  code VARCHAR(3) PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  symbol VARCHAR(10) NOT NULL,
  decimal_places INTEGER NOT NULL DEFAULT 2,
  is_active BOOLEAN NOT NULL DEFAULT true,
  is_default BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE exchange_rates (
  id BIGSERIAL PRIMARY KEY,
  from_currency VARCHAR(3) NOT NULL,
  to_currency VARCHAR(3) NOT NULL,
  rate DECIMAL(15,6) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT true,
  effective_date TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  created_by VARCHAR(100),
  FOREIGN KEY (from_currency) REFERENCES currencies(code),
  FOREIGN KEY (to_currency) REFERENCES currencies(code),
  UNIQUE(from_currency, to_currency)
);
```

## 🎯 Exemples d'Intégration

### Dans un tableau de données

```html
<table class="table">
  <thead>
    <tr>
      <th>Article</th>
      <th>Prix</th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let item of items">
      <td>{{ item.name }}</td>
      <td>
        <app-currency-display 
          [amount]="item.price" 
          [sourceCurrency]="item.currency"
          [showOriginal]="true">
        </app-currency-display>
      </td>
    </tr>
  </tbody>
</table>
```

### Dans un formulaire

```html
<div class="form-group">
  <label>Montant</label>
  <div class="input-group">
    <input type="number" class="form-control" [(ngModel)]="amount">
    <span class="input-group-text">
      {{ currencyService.getCurrentCurrency() }}
    </span>
  </div>
  <small class="form-text text-muted">
    Équivalent: {{ amount | currencyFormat:'USD' | async }}
  </small>
</div>
```

## 🔒 Sécurité

- Validation des taux de change (min: 0.000001)
- Authentification requise pour la mise à jour des taux
- Audit trail avec `created_by` et timestamps
- Protection CORS configurée

## 📈 Performance

- **Cache** : Devises et taux mis en cache côté service
- **Debounce** : Évite les conversions multiples rapides
- **Fallback** : Mode hors ligne avec taux locaux
- **Lazy Loading** : Composants chargés à la demande

## 🧪 Tests

### Initialisation des données de test

```bash
# Via API
POST /api/currencies/initialize

# Ou via SQL
INSERT INTO currencies VALUES 
('USD', 'Dollar Américain', '$', 2, true, true, NOW(), NULL),
('CDF', 'Franc Congolais', 'FC', 0, true, false, NOW(), NULL);

INSERT INTO exchange_rates VALUES 
(1, 'USD', 'CDF', 2700.000000, true, NOW(), NOW(), NULL, 'SYSTEM'),
(2, 'CDF', 'USD', 0.000370, true, NOW(), NOW(), NULL, 'SYSTEM');
```

### Tests unitaires

```typescript
// Test du service
it('should convert USD to CDF', () => {
  service.convert(100, 'USD', 'CDF').subscribe(result => {
    expect(result.convertedAmount).toBe(270000);
    expect(result.formattedAmount).toBe('270 000 FC');
  });
});
```

## 🚨 Dépannage

### Problèmes courants

1. **Taux non disponible** : Vérifier la table `exchange_rates`
2. **Conversion échoue** : Vérifier la connectivité backend
3. **Formatage incorrect** : Vérifier les `decimal_places` de la devise
4. **Cache obsolète** : Redémarrer l'application ou attendre 5 minutes

### Logs utiles

```bash
# Backend
2024-01-01 10:00:00 INFO  CurrencyService - Taux USD/CDF mis à jour: 2700.0
2024-01-01 10:00:00 ERROR CurrencyService - Erreur conversion: Taux non trouvé

# Frontend
[CurrencyService] Devise changée: CDF
[CurrencyService] Taux rechargés: 2 taux actifs
```

## 📞 Support

Pour toute question ou problème :
1. Vérifier cette documentation
2. Consulter les logs backend/frontend
3. Tester avec l'interface d'administration
4. Contacter l'équipe de développement

---

**Version :** 1.0.0  
**Dernière mise à jour :** 2024-01-01  
**Auteur :** Équipe FreightOps
