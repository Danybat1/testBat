import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-create-manifest',
  templateUrl: './create-manifest.component.html',
  styleUrls: ['./create-manifest.component.scss']
})
export class CreateManifestComponent implements OnInit {
  manifestForm: FormGroup;
  loading = false;

  constructor(private formBuilder: FormBuilder) {
    this.manifestForm = this.createForm();
  }

  ngOnInit(): void {
    // Component initialization
  }

  private createForm(): FormGroup {
    return this.formBuilder.group({
      manifestNumber: ['', Validators.required],
      date: ['', Validators.required],
      destination: ['', Validators.required],
      driver: ['', Validators.required],
      vehicle: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.manifestForm.valid) {
      this.loading = true;
      // TODO: Implement manifest creation logic
      console.log('Manifest creation:', this.manifestForm.value);
      setTimeout(() => {
        this.loading = false;
        alert('Fonctionnalité en développement');
      }, 1000);
    }
  }

  onCancel(): void {
    this.manifestForm.reset();
  }
}
