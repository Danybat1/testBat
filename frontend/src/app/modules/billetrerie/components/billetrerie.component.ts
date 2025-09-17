import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-billetrerie',
  templateUrl: './billetrerie.component.html',
  styleUrls: ['./billetrerie.component.scss']
})
export class BilleterieComponent implements OnInit {

  ticketStats = {
    total: 0,
    sold: 0,
    available: 0,
    revenue: 0
  };

  constructor() { }

  ngOnInit(): void {
    this.loadTicketStats();
  }

  private loadTicketStats(): void {
    // TODO: Implement ticket statistics loading
    this.ticketStats = {
      total: 150,
      sold: 89,
      available: 61,
      revenue: 45600
    };
  }

  onCreateTicket(): void {
    alert('Fonctionnalité de création de billets en développement');
  }

  onManageTickets(): void {
    alert('Fonctionnalité de gestion des billets en développement');
  }

}
