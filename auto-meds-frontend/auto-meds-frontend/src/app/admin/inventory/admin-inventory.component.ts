import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MedicineService } from '../../core/services/medicine.service';
import { AdminService, ProcurementAlert, DeficitSubscription, RestockRequest } from '../../core/services/admin.service';
import { Medicine } from '../../core/models/medicine.model';

@Component({
  selector: 'app-admin-inventory',
  templateUrl: './admin-inventory.component.html',
  styleUrls: ['./admin-inventory.component.css']
})
export class AdminInventoryComponent implements OnInit {
  activeTab: 'inventory' | 'procurement' = 'inventory';

  medicines: Medicine[] = [];
  filteredMedicines: Medicine[] = [];
  currentFilter: 'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' = 'ALL';
  loading = true;
  message = '';

  // Phase 3 Procurement & Deficit Queue State
  procurementAlerts: ProcurementAlert[] = [];
  deficitSubscriptions: DeficitSubscription[] = [];
  loadingProcurement = false;

  // 1-Click Restock Modal State
  isRestockModalOpen = false;
  selectedMedForRestock: { id: number; name: string; brand: string; suggestedPack?: number } | null = null;
  restockQuantity: number = 50;
  restockBatchNumber: string = '';
  restockExpiryDate: string = '';
  restockLoading = false;
  restockFeedback = '';

  constructor(
    private medicineService: MedicineService,
    private adminService: AdminService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['tab'] === 'procurement') {
        this.activeTab = 'procurement';
      }
    });

    this.loadInventory();
    this.loadProcurementData();
  }

  switchTab(tab: 'inventory' | 'procurement'): void {
    this.activeTab = tab;
    if (tab === 'procurement') {
      this.loadProcurementData();
    }
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

  loadProcurementData(): void {
    this.loadingProcurement = true;
    this.adminService.getProcurementAlerts().subscribe({
      next: (alerts) => {
        this.procurementAlerts = alerts;
        this.adminService.getDeficitSubscriptions().subscribe({
          next: (deficits) => {
            this.deficitSubscriptions = deficits;
            this.loadingProcurement = false;
          },
          error: () => this.loadingProcurement = false
        });
      },
      error: () => this.loadingProcurement = false
    });
  }

  applyFilter(filter: 'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK'): void {
    this.currentFilter = filter;
    if (filter === 'IN_STOCK') {
      this.filteredMedicines = this.medicines.filter(m => (m.availableQuantity ?? m.stockQuantity) > 0);
    } else if (filter === 'LOW_STOCK') {
      this.filteredMedicines = this.medicines.filter(m => (m.availableQuantity ?? m.stockQuantity) > 0 && (m.availableQuantity ?? m.stockQuantity) <= (m.reorderThreshold ?? 10));
    } else if (filter === 'OUT_OF_STOCK') {
      this.filteredMedicines = this.medicines.filter(m => (m.availableQuantity ?? m.stockQuantity) === 0);
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
        if (updated.availableQuantity !== undefined) {
          med.availableQuantity = updated.availableQuantity;
        }
        if (updated.reservedQuantity !== undefined) {
          med.reservedQuantity = updated.reservedQuantity;
        }
        this.message = `Stock updated for ${med.medicineName} (${med.brandName}).`;
        setTimeout(() => this.message = '', 3000);
      }
    });
  }

  openRestockModal(med: { id?: number; medicineId?: number; medicineName?: string; name?: string; brandName?: string; brand?: string; suggestedReorderPackSize?: number }): void {
    const defaultBatch = 'BATCH-' + new Date().getFullYear() + '-' + Math.floor(100 + Math.random() * 900);
    const oneYearLater = new Date();
    oneYearLater.setFullYear(oneYearLater.getFullYear() + 2);
    const defaultExpiry = oneYearLater.toISOString().split('T')[0];

    const targetId = med.id ?? med.medicineId ?? 0;
    this.selectedMedForRestock = {
      id: targetId,
      name: med.medicineName || med.name || 'Medicine',
      brand: med.brandName || med.brand || '',
      suggestedPack: med.suggestedReorderPackSize || 50
    };
    this.restockQuantity = this.selectedMedForRestock.suggestedPack || 50;
    this.restockBatchNumber = defaultBatch;
    this.restockExpiryDate = defaultExpiry;
    this.restockFeedback = '';
    this.isRestockModalOpen = true;
  }

  closeRestockModal(): void {
    this.isRestockModalOpen = false;
    this.selectedMedForRestock = null;
    this.restockFeedback = '';
  }

  submitRestock(): void {
    if (!this.selectedMedForRestock || this.restockQuantity <= 0 || this.restockLoading) return;

    this.restockLoading = true;
    const req: RestockRequest = {
      medicineId: this.selectedMedForRestock.id,
      quantity: this.restockQuantity,
      batchNumber: this.restockBatchNumber,
      expiryDate: this.restockExpiryDate
    };

    this.adminService.restockMedicine(req).subscribe({
      next: (res) => {
        this.restockLoading = false;
        this.message = `✅ ${res.message}`;
        this.closeRestockModal();
        this.loadInventory();
        this.loadProcurementData();
        setTimeout(() => this.message = '', 6000);
      },
      error: (err) => {
        this.restockLoading = false;
        this.restockFeedback = err.error?.detail || 'Failed to complete batch restock.';
      }
    });
  }
}
