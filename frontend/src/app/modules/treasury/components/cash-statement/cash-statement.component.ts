import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatTableDataSource } from '@angular/material/table';

interface CashOperation {
  id: number;
  operationDate: string;
  reference: string;
  operationType: string;
  amount: number;
  currency: string;
  paymentMethod: string;
  clientSupplier: string;
  description: string;
  balance: number;
}

interface CashStatement {
  operations: CashOperation[];
  openingBalance: number;
  closingBalance: number;
  totalEncaissements: number;
  totalDecaissements: number;
  startDate: string;
  endDate: string;
  currency: string;
  cashBoxName: string;
}

@Component({
  selector: 'app-cash-statement',
  templateUrl: './cash-statement.component.html',
  styleUrls: ['./cash-statement.component.scss']
})
export class CashStatementComponent implements OnInit {
  filterForm!: FormGroup;
  dataSource = new MatTableDataSource<CashOperation>([]);
  cashStatement: CashStatement | null = null;
  loading = false;

  displayedColumns: string[] = [
    'operationDate',
    'reference', 
    'operationType',
    'clientSupplier',
    'description',
    'encaissement',
    'decaissement',
    'balance'
  ];

  currencies = [
    { value: 'ALL', label: 'Toutes devises' },
    { value: 'CDF', label: 'CDF' },
    { value: 'USD', label: 'USD' },
    { value: 'EUR', label: 'EUR' }
  ];

  constructor(
    private fb: FormBuilder,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadDefaultStatement();
  }

  initializeForm(): void {
    const today = new Date();
    const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    
    this.filterForm = this.fb.group({
      startDate: [this.formatDateForInput(startOfMonth), Validators.required],
      endDate: [this.formatDateForInput(today), Validators.required],
      currency: ['CDF', Validators.required],
      cashBoxId: [1, Validators.required]
    });
  }

  loadDefaultStatement(): void {
    this.onFilterChange();
  }

  onFilterChange(): void {
    if (this.filterForm.valid) {
      this.loading = true;
      const filters = this.filterForm.value;
      
      const params = new URLSearchParams({
        startDate: filters.startDate,
        endDate: filters.endDate,
        currency: filters.currency,
        cashBoxId: filters.cashBoxId.toString()
      });

      console.log('Appel API avec paramètres:', params.toString());

      this.http.get<CashStatement>(`http://localhost:8080/api/cash-operations/statement?${params}`)
        .subscribe({
          next: (statement) => {
            console.log('Réponse API reçue:', statement);
            this.cashStatement = statement;
            this.dataSource.data = statement.operations;
            this.loading = false;
            
            // Afficher un message si aucune opération trouvée
            if (!statement.operations || statement.operations.length === 0) {
              console.warn('Aucune opération trouvée pour la période sélectionnée');
            }
          },
          error: (error) => {
            console.error('Erreur lors du chargement du relevé:', error);
            this.loading = false;
            // Ne pas charger les données mock, afficher l'erreur réelle
            alert('Erreur lors du chargement du relevé: ' + (error.error?.message || error.message));
          }
        });
    }
  }

  loadMockData(): void {
    // Données mock pour démonstration
    const mockOperations: CashOperation[] = [
      {
        id: 1,
        operationDate: '2024-01-15',
        reference: 'ENC-2024-001',
        operationType: 'ENCAISSEMENT',
        amount: 500000,
        currency: 'CDF',
        paymentMethod: 'ESPECES',
        clientSupplier: 'Client ABC',
        description: 'Paiement facture transport',
        balance: 500000
      },
      {
        id: 2,
        operationDate: '2024-01-15',
        reference: 'LTA-2024-001',
        operationType: 'ENCAISSEMENT_LTA',
        amount: 750000,
        currency: 'CDF',
        paymentMethod: 'ESPECES',
        clientSupplier: 'Expéditeur XYZ',
        description: 'Encaissement LTA KIN-GOM-001',
        balance: 1250000
      },
      {
        id: 3,
        operationDate: '2024-01-16',
        reference: 'DEC-2024-001',
        operationType: 'DECAISSEMENT',
        amount: 200000,
        currency: 'CDF',
        paymentMethod: 'ESPECES',
        clientSupplier: 'Fournisseur DEF',
        description: 'Achat carburant',
        balance: 1050000
      }
    ];

    this.cashStatement = {
      operations: mockOperations,
      openingBalance: 0,
      closingBalance: 1050000,
      totalEncaissements: 1250000,
      totalDecaissements: 200000,
      startDate: this.filterForm.get('startDate')?.value,
      endDate: this.filterForm.get('endDate')?.value,
      currency: 'CDF',
      cashBoxName: 'Caisse principale'
    };

    this.dataSource.data = mockOperations;
  }

  getOperationTypeLabel(type: string): string {
    const types: { [key: string]: string } = {
      'ENCAISSEMENT': 'Encaissement',
      'ENCAISSEMENT_LTA': 'Encaissement LTA',
      'DECAISSEMENT': 'Décaissement'
    };
    return types[type] || type;
  }

  getOperationTypeClass(type: string): string {
    switch (type) {
      case 'ENCAISSEMENT':
      case 'ENCAISSEMENT_LTA':
        return 'text-success';
      case 'DECAISSEMENT':
        return 'text-danger';
      default:
        return '';
    }
  }

  formatCurrency(amount: number, currency: string = 'CDF'): string {
    if (currency === 'CDF') {
      return new Intl.NumberFormat('fr-CD', {
        style: 'currency',
        currency: 'CDF',
        minimumFractionDigits: 0
      }).format(amount);
    } else {
      return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency: currency,
        minimumFractionDigits: 2
      }).format(amount);
    }
  }

  printStatement(): void {
    if (!this.cashStatement) {
      return;
    }

    const printContent = this.generateStatementHtml();
    const printWindow = window.open('', '_blank');
    
    if (printWindow) {
      printWindow.document.write(printContent);
      printWindow.document.close();
      printWindow.focus();
      
      // Wait for content to load then print
      setTimeout(() => {
        printWindow.print();
        printWindow.close();
      }, 250);
    }
  }

  exportToExcel(): void {
    if (!this.cashStatement) {
      return;
    }

    // Create CSV content
    const csvContent = this.generateCsvContent();
    
    // Create and download file
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `releve_caisse_${this.formatDateForFilename(this.cashStatement.startDate)}_${this.formatDateForFilename(this.cashStatement.endDate)}.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  private generateStatementHtml(): string {
    if (!this.cashStatement) return '';

    return `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>Relevé de Caisse - FreightOps</title>
        <style>
          body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background: white;
            color: #333;
            line-height: 1.6;
          }
          
          .header {
            text-align: center;
            border-bottom: 3px solid #1976d2;
            padding-bottom: 20px;
            margin-bottom: 30px;
          }
          
          .company-name {
            font-size: 28px;
            font-weight: bold;
            color: #1976d2;
            margin: 0;
          }
          
          .document-title {
            font-size: 24px;
            color: #333;
            margin: 10px 0;
          }
          
          .period-info {
            font-size: 16px;
            color: #666;
            margin: 5px 0;
          }
          
          .summary-section {
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
          }
          
          .summary-title {
            font-size: 18px;
            font-weight: bold;
            color: #1976d2;
            margin-bottom: 15px;
            border-bottom: 1px solid #1976d2;
            padding-bottom: 5px;
          }
          
          .summary-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
          }
          
          .summary-item {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
          }
          
          .summary-label {
            font-weight: 500;
          }
          
          .summary-value {
            font-weight: bold;
          }
          
          .opening-balance { color: #795548; }
          .encaissement { color: #4caf50; }
          .decaissement { color: #f44336; }
          .closing-balance { 
            color: #1976d2; 
            font-size: 18px;
            border-top: 2px solid #1976d2;
            padding-top: 10px;
            margin-top: 10px;
          }
          
          .operations-section {
            margin: 30px 0;
          }
          
          .section-title {
            font-size: 18px;
            font-weight: bold;
            color: #1976d2;
            margin-bottom: 15px;
            border-bottom: 1px solid #1976d2;
            padding-bottom: 5px;
          }
          
          .operations-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            font-size: 12px;
          }
          
          .operations-table th,
          .operations-table td {
            border: 1px solid #dee2e6;
            padding: 8px;
            text-align: left;
          }
          
          .operations-table th {
            background-color: #f8f9fa;
            font-weight: bold;
            color: #333;
          }
          
          .operations-table tr:nth-child(even) {
            background-color: #f8f9fa;
          }
          
          .amount-cell {
            text-align: right;
            font-weight: 500;
          }
          
          .encaissement-amount { color: #4caf50; }
          .decaissement-amount { color: #f44336; }
          .balance-amount { color: #1976d2; font-weight: bold; }
          
          .footer {
            margin-top: 40px;
            border-top: 1px solid #dee2e6;
            padding-top: 20px;
            text-align: center;
            color: #666;
            font-size: 12px;
          }
          
          .signature-section {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 50px;
            margin-top: 40px;
          }
          
          .signature-box {
            text-align: center;
            border-top: 1px solid #333;
            padding-top: 10px;
          }
          
          @media print {
            body { margin: 0; }
            .operations-table { font-size: 10px; }
            .operations-table th,
            .operations-table td { padding: 4px; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1 class="company-name">FreightOps</h1>
          <h2 class="document-title">Relevé de Caisse</h2>
          <p class="period-info">
            Période: ${this.formatDate(this.cashStatement.startDate)} - ${this.formatDate(this.cashStatement.endDate)}
          </p>
          <p class="period-info">
            Devise: ${this.cashStatement.currency} | 
            Généré le: ${this.formatDate(new Date())} à ${new Date().toLocaleTimeString('fr-FR')}
          </p>
        </div>

        <div class="summary-section">
          <div class="summary-title">Résumé de la période</div>
          <div class="summary-grid">
            <div class="summary-item">
              <span class="summary-label">Solde d'ouverture:</span>
              <span class="summary-value opening-balance">${this.formatCurrency(this.cashStatement.openingBalance)}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Total encaissements:</span>
              <span class="summary-value encaissement">${this.formatCurrency(this.cashStatement.totalEncaissements)}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Total décaissements:</span>
              <span class="summary-value decaissement">${this.formatCurrency(this.cashStatement.totalDecaissements)}</span>
            </div>
            <div class="summary-item closing-balance">
              <span class="summary-label">Solde de clôture:</span>
              <span class="summary-value">${this.formatCurrency(this.cashStatement.closingBalance)}</span>
            </div>
          </div>
        </div>

        <div class="operations-section">
          <div class="section-title">Détail des opérations (${this.cashStatement.operations.length} opération${this.cashStatement.operations.length > 1 ? 's' : ''})</div>
          
          <table class="operations-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Référence</th>
                <th>Type</th>
                <th>Client/Fournisseur</th>
                <th>Description</th>
                <th>Encaissement</th>
                <th>Décaissement</th>
                <th>Solde</th>
              </tr>
            </thead>
            <tbody>
              ${this.cashStatement.operations.map(operation => `
                <tr>
                  <td>${this.formatDate(operation.operationDate)}</td>
                  <td>${operation.reference}</td>
                  <td>${this.getOperationTypeLabel(operation.operationType)}</td>
                  <td>${operation.clientSupplier}</td>
                  <td>${operation.description}</td>
                  <td class="amount-cell ${operation.operationType.includes('ENCAISSEMENT') ? 'encaissement-amount' : ''}">
                    ${operation.operationType.includes('ENCAISSEMENT') ? this.formatCurrency(operation.amount, operation.currency) : ''}
                  </td>
                  <td class="amount-cell ${operation.operationType === 'DECAISSEMENT' ? 'decaissement-amount' : ''}">
                    ${operation.operationType === 'DECAISSEMENT' ? this.formatCurrency(operation.amount, operation.currency) : ''}
                  </td>
                  <td class="amount-cell balance-amount">${this.formatCurrency(operation.balance, operation.currency)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>

        <div class="signature-section">
          <div class="signature-box">
            <p>Caissier</p>
            <br><br>
            <p>Signature: _________________</p>
          </div>
          <div class="signature-box">
            <p>Responsable</p>
            <br><br>
            <p>Signature: _________________</p>
          </div>
        </div>

        <div class="footer">
          <p>FreightOps - Système de Gestion du Transport Aérien</p>
          <p>Document généré automatiquement - ${new Date().toLocaleString('fr-FR')}</p>
        </div>
      </body>
      </html>
    `;
  }

  private generateCsvContent(): string {
    if (!this.cashStatement) return '';

    let csvContent = '\uFEFF'; // UTF-8 BOM for Excel compatibility
    
    // Header information
    csvContent += `FreightOps - Relevé de Caisse\n`;
    csvContent += `Période:,${this.formatDate(this.cashStatement.startDate)} - ${this.formatDate(this.cashStatement.endDate)}\n`;
    csvContent += `Devise:,${this.cashStatement.currency}\n`;
    csvContent += `Généré le:,${this.formatDate(new Date())} ${new Date().toLocaleTimeString('fr-FR')}\n\n`;
    
    // Summary section
    csvContent += `RÉSUMÉ DE LA PÉRIODE\n`;
    csvContent += `Solde d'ouverture:,${this.cashStatement.openingBalance}\n`;
    csvContent += `Total encaissements:,${this.cashStatement.totalEncaissements}\n`;
    csvContent += `Total décaissements:,${this.cashStatement.totalDecaissements}\n`;
    csvContent += `Solde de clôture:,${this.cashStatement.closingBalance}\n\n`;
    
    // Operations header
    csvContent += `DÉTAIL DES OPÉRATIONS\n`;
    csvContent += `Date,Référence,Type,Client/Fournisseur,Description,Encaissement,Décaissement,Solde\n`;
    
    // Operations data
    this.cashStatement.operations.forEach(operation => {
      const encaissement = operation.operationType.includes('ENCAISSEMENT') ? operation.amount : '';
      const decaissement = operation.operationType === 'DECAISSEMENT' ? operation.amount : '';
      
      csvContent += `${this.formatDate(operation.operationDate)},`;
      csvContent += `"${operation.reference}",`;
      csvContent += `"${this.getOperationTypeLabel(operation.operationType)}",`;
      csvContent += `"${operation.clientSupplier}",`;
      csvContent += `"${operation.description}",`;
      csvContent += `${encaissement},`;
      csvContent += `${decaissement},`;
      csvContent += `${operation.balance}\n`;
    });
    
    return csvContent;
  }

  private formatDateForFilename(date: Date | string): string {
    const d = new Date(date);
    return d.toISOString().split('T')[0].replace(/-/g, '');
  }

  private formatDateForInput(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  private formatDate(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}
