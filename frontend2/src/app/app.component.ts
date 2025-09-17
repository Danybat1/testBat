import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="app-container">
      <header class="header">
        <div class="container">
          <h1 class="logo">
            <span class="logo-icon">📦</span>
            FreightOps
          </h1>
          <p class="tagline">Suivi de colis en temps réel</p>
        </div>
      </header>
      
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
      
      <footer class="footer">
        <div class="container">
          <p>&copy; 2024 FreightOps. Tous droits réservés.</p>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .header {
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
      padding: 2rem 0;
      text-align: center;
      color: white;
    }

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1rem;
    }

    .logo {
      font-size: 2.5rem;
      font-weight: 300;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }

    .logo-icon {
      font-size: 3rem;
    }

    .tagline {
      font-size: 1.1rem;
      margin: 0.5rem 0 0 0;
      opacity: 0.9;
    }

    .main-content {
      flex: 1;
      padding: 2rem 0;
    }

    .footer {
      background: rgba(0, 0, 0, 0.2);
      color: white;
      text-align: center;
      padding: 1rem 0;
    }

    .footer p {
      margin: 0;
      opacity: 0.8;
    }
  `]
})
export class AppComponent {
  title = 'FreightOps - Suivi Public';
}
