import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Tax {
  id?: number;
  name: string;
  rate: number;
  description?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TaxService {
  private apiUrl = `${environment.apiUrl}/api/taxes`;

  constructor(private http: HttpClient) {}

  getAllTaxes(): Observable<Tax[]> {
    return this.http.get<Tax[]>(this.apiUrl);
  }

  getActiveTaxes(): Observable<Tax[]> {
    return this.http.get<Tax[]>(`${this.apiUrl}/active`);
  }

  getTaxById(id: number): Observable<Tax> {
    return this.http.get<Tax>(`${this.apiUrl}/${id}`);
  }

  getTaxByName(name: string): Observable<Tax> {
    return this.http.get<Tax>(`${this.apiUrl}/name/${name}`);
  }

  createTax(tax: Tax): Observable<Tax> {
    return this.http.post<Tax>(this.apiUrl, tax);
  }

  updateTax(id: number, tax: Tax): Observable<Tax> {
    return this.http.put<Tax>(`${this.apiUrl}/${id}`, tax);
  }

  deleteTax(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  activateTax(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivateTax(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }
}
