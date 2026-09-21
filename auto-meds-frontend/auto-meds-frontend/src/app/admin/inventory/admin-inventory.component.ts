import { Component, OnInit } from '@angular/core';
import { MedicineService } from '../../core/services/medicine.service';
import { AdminService } from '../../core/services/admin.service';
import { Medicine } from '../../core/models/medicine.model';

@Component({
  selector: 'app-admin-inventory',
  templateUrl: './admin-inventory.component.html',
  styleUrls: ['./admin-inventory.component.css']
})
export class AdminInventoryComponent implements OnInit {
  medicines: Medicine[] = [];
  filteredMedicines: Medicine[] = [];
  currentFilter: 'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' = 'ALL';
  loading = true;
  message = '';

  constructor(private medicineService: MedicineService, private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadInventory();
  }

  loadInventory(): void {
    this.loading = true;
    this.medicineService.getMedicines().subscribe({
      next: (data) => {
        this.medicines = data;
        this.applyFilter(this.currentFilter);
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  applyFilter(filter: 'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK'): void {
    this.currentFilter = filter;
    if (filter === 'IN_STOCK') {
      this.filteredMedicines = this.medicines.filter(m => m.stockQuantity > 0);
    } else if (filter === 'LOW_STOCK') {
      this.filteredMedicines = this.medicines.filter(m => m.stockQuantity > 0 && m.stockQuantity <= 10);
    } else if (filter === 'OUT_OF_STOCK') {
      this.filteredMedicines = this.medicines.filter(m => m.stockQuantity === 0);
    } else {
      this.filteredMedicines = [...this.medicines];
    }
  }

  updateStock(med: Medicine, change: number): void {
    const newStock = med.stockQuantity + change;
    if (newStock < 0) return;

    this.adminService.updateStock(med.id, newStock).subscribe({
      next: (updated) => {
        med.stockQuantity = updated.stockQuantity;
        this.message = `Stock updated for ${med.medicineName} (${med.brandName}).`;
        setTimeout(() => this.message = '', 3000);
      }
    });
  }
}
