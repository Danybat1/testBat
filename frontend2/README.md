# FreightOps - Site Public de Suivi

Site vitrine public pour le suivi de colis FreightOps, accessible sans authentification.

## 🚀 Démarrage Rapide

```bash
# Installation des dépendances
npm install

# Démarrage en mode développement (port 4201)
npm start

# Build de production
npm run build
```

## 📋 Fonctionnalités

- ✅ Suivi de colis par numéro de tracking
- ✅ Interface moderne et responsive
- ✅ Affichage du statut en temps réel
- ✅ Barre de progression visuelle
- ✅ Informations détaillées du colis
- ✅ Design Material Design

## 🏗️ Architecture

- **Framework**: Angular 17+ (Standalone Components)
- **UI**: Angular Material
- **Styles**: SCSS avec thème personnalisé
- **API**: Consomme l'API publique `/api/public/tracking/{trackingNumber}`

## 🌐 URLs

- **Développement**: http://localhost:4201
- **API Backend**: http://localhost:8080

## 📱 Responsive Design

Le site s'adapte automatiquement aux différentes tailles d'écran :
- Desktop (> 768px)
- Tablet (768px - 1024px) 
- Mobile (< 768px)

## 🔧 Configuration

Modifier `src/environments/environment.ts` pour changer l'URL de l'API :

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

## 🎨 Personnalisation

- **Couleurs**: Modifier `src/styles.scss`
- **Logo**: Remplacer l'emoji dans `app.component.ts`
- **Thème**: Changer le thème Material dans `angular.json`
