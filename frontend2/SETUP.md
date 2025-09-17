# Guide de Configuration Frontend2 - FreightOps Public

## 🔧 Installation et Démarrage

### Prérequis
- Node.js 18+ 
- npm ou yarn
- Angular CLI 17+

### Installation
```bash
cd C:\Users\DON\Desktop\Efret\frontend2

# Installation des dépendances
npm install

# Démarrage en développement
npm start
```

### Vérification
L'application sera accessible sur : http://localhost:4201

## 🚨 Résolution des Erreurs Communes

### 1. Erreur de compilation Angular
```bash
# Nettoyer le cache Angular
npx ng cache clean

# Réinstaller les dépendances
rm -rf node_modules package-lock.json
npm install
```

### 2. Erreur Material Design
```bash
# Vérifier que Angular Material est installé
npm list @angular/material @angular/cdk
```

### 3. Erreur de port occupé
```bash
# Changer le port
ng serve --port 4202
```

### 4. Erreur CORS
- Vérifier que le backend est démarré sur port 8080
- Vérifier la configuration CORS dans SecurityConfig.java

## 📁 Structure Corrigée

```
frontend2/
├── src/
│   ├── app/
│   │   ├── components/tracking/
│   │   ├── models/
│   │   ├── services/
│   │   ├── app.component.ts
│   │   ├── app.config.ts
│   │   └── app.routes.ts
│   ├── assets/
│   ├── environments/
│   ├── index.html
│   ├── main.ts
│   └── styles.scss
├── angular.json ✅
├── package.json ✅
├── tsconfig.json ✅
├── karma.conf.js ✅
└── .gitignore ✅
```

## ✅ Corrections Appliquées

1. **Dépendances** : Angular 17.3 + Angular CDK
2. **Configuration** : angular.json, tsconfig, karma
3. **Fichiers manquants** : favicon.ico, assets/, environments/
4. **Structure** : Tous les composants et services créés
5. **Styles** : Material Design + thème personnalisé

## 🧪 Tests

```bash
# Tests unitaires
npm test

# Build de production
npm run build
```

## 🔗 Intégration Backend

L'application consomme l'API publique :
- Endpoint : `GET /api/public/tracking/{trackingNumber}`
- URL Backend : http://localhost:8080
- Configuration : `src/environments/environment.ts`
