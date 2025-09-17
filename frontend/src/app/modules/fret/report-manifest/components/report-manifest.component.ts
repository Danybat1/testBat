import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ManifestService, ManifestResponse } from '../services/manifest.service';

interface ManifestListItem {
  id: number;
  manifestNumber: string;
  proformaNumber: string;
  createdDate: Date;
  transportMode: string;
  status: string;
  parties?: any[];
  goods?: any[];
}

@Component({
  selector: 'app-report-manifest',
  templateUrl: './report-manifest.component.html',
  styleUrls: ['./report-manifest.component.scss']
})
export class ReportManifestComponent implements OnInit {
  manifests: ManifestListItem[] = [];
  filteredManifests: ManifestListItem[] = [];
  isLoading = false;
  error: string | null = null;
  
  // Filters
  searchTerm = '';
  selectedStatus = '';
  selectedTransportMode = '';
  
  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  totalItems = 0;

  constructor(
    private router: Router, 
    private route: ActivatedRoute,
    private manifestService: ManifestService
  ) {}

  ngOnInit(): void {
    this.loadManifests();
  }

  loadManifests(): void {
    this.isLoading = true;
    this.error = null;
    
    this.manifestService.getAllManifests().subscribe({
      next: (manifests) => {
        // Vérifier que manifests est un array
        const manifestsArray = Array.isArray(manifests) ? manifests : [];
        
        this.manifests = manifestsArray.map(m => ({
          id: m.id || 0,
          manifestNumber: m.manifestNumber || '',
          proformaNumber: m.proformaNumber || '',
          createdDate: new Date(m.createdDate || Date.now()),
          transportMode: m.transportMode || '',
          status: m.status || 'DRAFT',
          parties: m.parties || [],
          goods: m.goods || []
        }));
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des manifestes:', error);
        this.error = 'Erreur lors du chargement des manifestes';
        this.isLoading = false;
        // Données de test en cas d'erreur
        this.loadTestData();
      }
    });
  }

  private loadTestData(): void {
    // Inclure les manifestes créés récemment
    this.manifests = [
      {
        id: 1,
        manifestNumber: 'MAN-1757156003353',
        proformaNumber: '195',
        createdDate: new Date(),
        transportMode: 'SEA',
        status: 'CONFIRMED',
        parties: [
          { partyType: 'SHIPPER', companyName: 'Export Maritime SARL' },
          { partyType: 'CONSIGNEE', companyName: 'Import Ocean Ltd' }
        ],
        goods: []
      },
      {
        id: 2,
        manifestNumber: 'MAN-1757154498880',
        proformaNumber: '748',
        createdDate: new Date(Date.now() - 86400000), // Hier
        transportMode: 'ROAD',
        status: 'CONFIRMED',
        parties: [
          { partyType: 'SHIPPER', companyName: 'Transport Routier SA' },
          { partyType: 'CONSIGNEE', companyName: 'Logistique Express' }
        ],
        goods: []
      }
    ];
    this.applyFilters();
  }

  /**
   * Rafraîchir la liste après création d'un nouveau manifeste
   */
  refreshAfterCreate(): void {
    this.loadManifests();
  }

  onSearch(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onClearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.selectedTransportMode = '';
    this.currentPage = 1;
    this.applyFilters();
  }

  private applyFilters(): void {
    let filtered = [...this.manifests];

    // Recherche textuelle
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(m => 
        m.manifestNumber.toLowerCase().includes(term) ||
        m.proformaNumber.toLowerCase().includes(term)
      );
    }

    // Filtre par statut
    if (this.selectedStatus) {
      filtered = filtered.filter(m => m.status === this.selectedStatus);
    }

    // Filtre par mode de transport
    if (this.selectedTransportMode) {
      filtered = filtered.filter(m => m.transportMode === this.selectedTransportMode);
    }

    this.filteredManifests = filtered;
    this.totalItems = filtered.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    
    // Pagination
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.filteredManifests = filtered.slice(startIndex, endIndex);
  }

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('fr-FR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).format(date);
  }

  getTransportModeLabel(mode: string): string {
    const labels: { [key: string]: string } = {
      'ROAD': 'Routier',
      'AIR': 'Aérien',
      'SEA': 'Maritime',
      'RAIL': 'Ferroviaire',
      'MULTIMODAL': 'Multimodal'
    };
    return labels[mode] || mode;
  }

  getTransportModeClass(mode: string): string {
    const classes: { [key: string]: string } = {
      'ROAD': 'badge-warning',
      'AIR': 'badge-info',
      'SEA': 'badge-primary',
      'RAIL': 'badge-secondary',
      'MULTIMODAL': 'badge-dark'
    };
    return classes[mode] || 'badge-light';
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'DRAFT': 'Brouillon',
      'CONFIRMED': 'Confirmé',
      'IN_TRANSIT': 'En transit',
      'DELIVERED': 'Livré'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'DRAFT': 'badge-secondary',
      'CONFIRMED': 'badge-primary',
      'IN_TRANSIT': 'badge-warning',
      'DELIVERED': 'badge-success'
    };
    return classes[status] || 'badge-light';
  }

  getShipperName(manifest: ManifestListItem): string {
    const shipper = manifest.parties?.find(p => p.partyType === 'SHIPPER');
    return shipper?.companyName || 'Non défini';
  }

  getConsigneeName(manifest: ManifestListItem): string {
    const consignee = manifest.parties?.find(p => p.partyType === 'CONSIGNEE');
    return consignee?.companyName || 'Non défini';
  }

  canEdit(manifest: ManifestListItem): boolean {
    return manifest.status === 'DRAFT' || manifest.status === 'CONFIRMED';
  }

  onViewManifest(manifest: ManifestListItem): void {
    this.router.navigate(['view', manifest.id], { relativeTo: this.route });
  }

  onPrintPDF(manifest: ManifestListItem): void {
    this.isLoading = true;
    this.manifestService.generatePDF(manifest.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${manifest.manifestNumber}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur génération PDF:', error);
        this.error = 'Erreur lors de la génération du PDF';
        this.isLoading = false;
      }
    });
  }

  onPrintWord(manifest: ManifestListItem): void {
    this.isLoading = true;
    this.manifestService.generateWord(manifest.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${manifest.manifestNumber}.rtf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur génération Word:', error);
        this.error = 'Erreur lors de la génération du document Word';
        this.isLoading = false;
      }
    });
  }

  onEditManifest(manifest: ManifestListItem): void {
    this.router.navigate(['edit', manifest.id], { relativeTo: this.route });
  }

  onCreateNewManifest(): void {
    this.router.navigate(['create'], { relativeTo: this.route });
  }

  onRefresh(): void {
    this.loadManifests();
  }

  onPageChange(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.applyFilters();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible - 1);
    
    if (end - start + 1 < maxVisible) {
      start = Math.max(1, end - maxVisible + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }
}
