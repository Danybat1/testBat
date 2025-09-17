import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HelloService } from '../../services/hello.service';

@Component({
  selector: 'app-hello',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hello.component.html',
  //styleUrls: ['./hello.component.css']
})
export class HelloComponent implements OnInit {

  backendMessage: string = '';

  constructor(private helloService: HelloService) {}

  ngOnInit(): void {
    this.helloService.getMessage().subscribe({
      next: (message) => this.backendMessage = message,
      error: (err) => console.error('Erreur de connexion au backend', err)
    });
  }
}
