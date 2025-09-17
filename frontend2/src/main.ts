import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => {
    console.error('Erreur lors du démarrage de l\'application:', err);
    
    // Afficher une erreur utilisateur si possible
    const errorDiv = document.createElement('div');
    errorDiv.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: #f44336;
      color: white;
      padding: 20px;
      border-radius: 8px;
      font-family: Arial, sans-serif;
      z-index: 9999;
      max-width: 400px;
      text-align: center;
    `;
    errorDiv.innerHTML = `
      <h3>Erreur de démarrage</h3>
      <p>L'application n'a pas pu démarrer correctement.</p>
      <p>Vérifiez la console pour plus de détails.</p>
    `;
    document.body.appendChild(errorDiv);
  });
