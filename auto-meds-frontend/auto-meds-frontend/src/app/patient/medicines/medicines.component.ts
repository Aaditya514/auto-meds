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

  // Symptom Discovery Filters
  activeSymptom: string = '';
  symptomCategories = [
    { label: 'All Medicines', icon: 'bi-grid-fill', value: '' },
    { label: 'Fever & Pain', icon: 'bi-thermometer-half', value: 'fever' },
    { label: 'Headache & Migraine', icon: 'bi-bandaid-fill', value: 'headache' },
    { label: 'Acidity & Heartburn', icon: 'bi-fire', value: 'acidity' },
    { label: 'Allergy & Cold', icon: 'bi-flower1', value: 'allergy' },
    { label: 'Diabetes Care', icon: 'bi-heart-pulse-fill', value: 'diabetes' },
    { label: 'Hypertension & BP', icon: 'bi-speedometer2', value: 'hypertension' }
  ];

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

  selectSymptom(symptomValue: string): void {
    this.activeSymptom = symptomValue;
    this.searchQuery = '';
    if (!symptomValue) {
      this.loadMedicines();
      return;
    }
    this.loading = true;
    this.medicineService.getMedicinesBySymptom(symptomValue).subscribe({
      next: (data) => {
        this.medicines = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to filter medicines by symptom.';
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.activeSymptom = '';
    if (!this.searchQuery || this.searchQuery.trim() === '') {
      this.loadMedicines();
      return;
    }
    this.loading = true;
    this.medicineService.searchMedicines(this.searchQuery.trim()).subscribe({
      next: (data) => {
        this.medicines = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  switchToGeneric(genericId: number): void {
    this.loading = true;
    this.medicineService.getMedicineById(genericId).subscribe({
      next: (med) => {
        this.medicines = [med];
        this.searchQuery = med.medicineName;
        this.loading = false;
        this.message = `Showing recommended low-cost generic alternative: ${med.medicineName}`;
        setTimeout(() => this.message = '', 5000);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Could not load generic medicine details.';
      }
    });
  }

  onSearchChange(): void {
    if (!this.searchQuery || this.searchQuery.trim() === '') {
      this.loadMedicines();
    }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadMedicines();
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
