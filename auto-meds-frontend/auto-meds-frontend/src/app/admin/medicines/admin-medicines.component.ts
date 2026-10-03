import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MedicineService } from '../../core/services/medicine.service';
import { AdminService } from '../../core/services/admin.service';
import { Medicine } from '../../core/models/medicine.model';

@Component({
  selector: 'app-admin-medicines',
  templateUrl: './admin-medicines.component.html',
  styleUrls: ['./admin-medicines.component.css']
})
export class AdminMedicinesComponent implements OnInit {
  medicines: Medicine[] = [];
  medicineForm!: FormGroup;
  loading = true;
  submitting = false;
  showModal = false;
  editingMedicineId: number | null = null;
  message = '';
  errorMessage = '';
  searchQuery = '';

  get filteredMedicines(): Medicine[] {
    if (!this.searchQuery || !this.searchQuery.trim()) {
      return this.medicines;
    }
    const q = this.searchQuery.toLowerCase().trim();
    return this.medicines.filter(m => 
      m.medicineName?.toLowerCase().includes(q) ||
      m.brandName?.toLowerCase().includes(q) ||
      m.composition?.toLowerCase().includes(q) ||
      m.category?.toLowerCase().includes(q) ||
      m.symptoms?.toLowerCase().includes(q) ||
      String(m.id).includes(q)
    );
  }

  // Disease Classification & Tagging State
  diseaseTags: string[] = [];
  currentTagInput: string = '';
  
  presetDiseaseSuggestions: string[] = [
    'Type 2 Diabetes',
    'High Blood Sugar',
    'Hypertension',
    'High Blood Pressure',
    'Acidity & GERD',
    'Heartburn & Reflux',
    'Fever & Pain',
    'Headache & Migraine',
    'High Cholesterol',
    'Allergy & Cold',
    'GLP-1 / Weight Loss',
    'Asthma & Bronchitis',
    'Joint & Muscle Pain',
    'Bacterial Infection'
  ];

  therapeuticCategories: string[] = [
    'Diabetes Care',
    'Cardiovascular & Hypertension',
    'GLP-1 & Metabolic Care',
    'Gastrointestinal & Acidity',
    'Analgesic & Pain Relief',
    'Respiratory & Allergy',
    'Antibiotics & Anti-Infective',
    'Cholesterol & Lipid Care',
    'Vitamins & Supplements',
    'General Health & Wellness'
  ];

  constructor(
    private formBuilder: FormBuilder,
    private medicineService: MedicineService,
    private adminService: AdminService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadMedicines();
  }

  initForm(): void {
    this.diseaseTags = [];
    this.currentTagInput = '';
    this.medicineForm = this.formBuilder.group({
      medicineName: ['', Validators.required],
      brandName: ['', Validators.required],
      composition: ['', Validators.required],
      strength: ['', Validators.required],
      category: [''],
      price: [null, [Validators.required, Validators.min(0)]],
      stockQuantity: [null, [Validators.required, Validators.min(0)]],
      requiresPrescription: [false],
      description: [''],
      manufacturer: ['']
    });
  }

  loadMedicines(): void {
    this.loading = true;
    this.medicineService.getMedicines().subscribe({
      next: (data) => {
        this.medicines = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openAddModal(): void {
    this.editingMedicineId = null;
    this.initForm();
    this.showModal = true;
  }

  openEditModal(med: Medicine): void {
    this.editingMedicineId = med.id;
    this.diseaseTags = med.symptoms 
      ? med.symptoms.split(',').map(s => s.trim()).filter(s => s.length > 0)
      : [];
    this.currentTagInput = '';

    this.medicineForm.patchValue({
      medicineName: med.medicineName,
      brandName: med.brandName,
      composition: med.composition,
      strength: med.strength,
      category: med.category || 'General',
      price: med.price,
      stockQuantity: med.stockQuantity,
      requiresPrescription: med.requiresPrescription,
      description: med.description || '',
      manufacturer: med.manufacturer || ''
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingMedicineId = null;
    this.diseaseTags = [];
    this.currentTagInput = '';
  }

  addTag(tagToAdd?: string): void {
    const raw = tagToAdd !== undefined ? tagToAdd : this.currentTagInput;
    if (!raw) return;
    const clean = raw.trim();
    if (clean.length > 0 && !this.diseaseTags.includes(clean)) {
      this.diseaseTags.push(clean);
    }
    if (tagToAdd === undefined) {
      this.currentTagInput = '';
    }
  }

  removeTag(index: number): void {
    if (index >= 0 && index < this.diseaseTags.length) {
      this.diseaseTags.splice(index, 1);
    }
  }

  addPresetTag(preset: string): void {
    this.addTag(preset);
  }

  getSymptomsList(symptoms?: string): string[] {
    if (!symptoms) return [];
    return symptoms.split(',').map(s => s.trim()).filter(s => s.length > 0);
  }

  onSubmit(): void {
    if (this.medicineForm.invalid) return;

    this.submitting = true;
    const val = {
      ...this.medicineForm.value,
      symptoms: this.diseaseTags.join(', ')
    };

    if (this.editingMedicineId) {
      this.adminService.updateMedicine(this.editingMedicineId, val).subscribe({
        next: () => {
          this.submitting = false;
          this.message = 'Medicine updated successfully.';
          this.closeModal();
          this.loadMedicines();
        },
        error: (err) => {
          this.errorMessage = err.message;
          this.submitting = false;
        }
      });
    } else {
      this.adminService.createMedicine(val).subscribe({
        next: () => {
          this.submitting = false;
          this.message = 'New medicine added successfully.';
          this.closeModal();
          this.loadMedicines();
        },
        error: (err) => {
          this.errorMessage = err.message;
          this.submitting = false;
        }
      });
    }
  }

  deactivate(id: number): void {
    if (confirm('Are you sure you want to deactivate this medicine?')) {
      this.adminService.deactivateMedicine(id).subscribe({
        next: () => {
          this.message = 'Medicine deactivated.';
          this.loadMedicines();
        }
      });
    }
  }
}
