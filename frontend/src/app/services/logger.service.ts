import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  constructor() { }

  /**
   * Log général - affiché uniquement en développement
   */
  log(message: string, ...optionalParams: any[]): void {
    if (!environment.production) {
      console.log(`[LOG] ${new Date().toLocaleTimeString()} - ${message}`, ...optionalParams);
    }
  }

  /**
   * Debug technique - affiché uniquement en développement
   */
  debug(message: string, ...optionalParams: any[]): void {
    if (!environment.production) {
      console.debug(`[DEBUG] ${new Date().toLocaleTimeString()} - ${message}`, ...optionalParams);
    }
  }

  /**
   * Avertissements - affiché dans tous les environnements
   */
  warn(message: string, ...optionalParams: any[]): void {
    console.warn(`[WARN] ${new Date().toLocaleTimeString()} - ${message}`, ...optionalParams);
  }

  /**
   * Erreurs - affiché dans tous les environnements
   */
  error(message: string, error?: any, ...optionalParams: any[]): void {
    console.error(`[ERROR] ${new Date().toLocaleTimeString()} - ${message}`, error, ...optionalParams);
  }

  /**
   * Log d'information importante - affiché dans tous les environnements
   */
  info(message: string, ...optionalParams: any[]): void {
    console.info(`[INFO] ${new Date().toLocaleTimeString()} - ${message}`, ...optionalParams);
  }

  /**
   * Groupe de logs pour organiser les messages liés
   */
  group(label: string): void {
    if (!environment.production) {
      console.group(`[GROUP] ${new Date().toLocaleTimeString()} - ${label}`);
    }
  }

  /**
   * Fin du groupe de logs
   */
  groupEnd(): void {
    if (!environment.production) {
      console.groupEnd();
    }
  }

  /**
   * Log avec table pour afficher des données structurées
   */
  table(data: any, label?: string): void {
    if (!environment.production) {
      if (label) {
        console.log(`[TABLE] ${new Date().toLocaleTimeString()} - ${label}`);
      }
      console.table(data);
    }
  }
}
