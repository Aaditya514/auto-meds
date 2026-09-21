import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MedicineService } from '../../core/services/medicine.service';
import { CartService } from '../../core/services/cart.service';
import { Medicine } from '../../core/models/medicine.model';

@Component({
  selector: 'app-medicines',
  templateUrl: './medicines.component.html',
  styleUrls: ['./medicines.component.css']
})
export class MedicinesComponent implements OnInit {
  medicines: Medicine[] = [];
  searchQuery: string = '';
  loading = true;
  message = '';
  errorMessage = '';

  // Alternative Modal State
  selectedOutMedicine: Medicine | null = null;
  alternativeMedicines: Medicine[] = [];
  loadingAlternatives = false;

  constructor(
    private medicineService: MedicineService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMedicines();
  }

  loadMedicines(): void {
    this.loading = true;
    this.medicineService.getMedicines().subscribe({
      next: (data) => {
        this.medicines = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load medicines.';
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.loading = true;
    this.medicineService.searchMedicines(this.searchQuery).subscribe({
      next: (data) => {
        this.medicines = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  addToCart(medicine: Medicine): void {
    this.message = '';
    this.errorMessage = '';
    this.cartService.addItem(medicine.id, 1).subscribe({
      next: () => {
        this.message = `Added ${medicine.medicineName} (${medicine.brandName}) to cart!`;
        setTimeout(() => this.message = '', 4000);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to add item to cart.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  openAlternativesModal(medicine: Medicine): void {
    this.selectedOutMedicine = medicine;
    this.alternativeMedicines = [];
    this.loadingAlternatives = true;

    this.medicineService.getAlternativeMedicines(medicine.id).subscribe({
      next: (data) => {
        this.alternativeMedicines = data;
        this.loadingAlternatives = false;
      },
      error: () => {
        this.loadingAlternatives = false;
      }
    });
  }

  closeAlternativesModal(): void {
    this.selectedOutMedicine = null;
    this.alternativeMedicines = [];
  }

  navigateToSubscribe(medicineId: number): void {
    this.closeAlternativesModal();
    this.router.navigate(['/subscriptions/create'], { queryParams: { medicineId } });
  }
}
