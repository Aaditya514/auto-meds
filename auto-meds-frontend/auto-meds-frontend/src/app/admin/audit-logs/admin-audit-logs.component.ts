import { Component, OnInit } from '@angular/core';
import { ComplianceAndPaymentService } from '../../core/services/compliance-and-payment.service';
import { AuditLog } from '../../core/models/compliance-and-payment.model';

@Component({
  selector: 'app-admin-audit-logs',
  templateUrl: './admin-audit-logs.component.html',
  styleUrls: ['./admin-audit-logs.component.css']
})
export class AdminAuditLogsComponent implements OnInit {
  logs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  loading = true;
  selectedAction = 'ALL';
  searchTerm = '';

  actionFilters: string[] = [
    'ALL',
    'PHARMACIST_APPROVAL',
    'PAYMENT_CAPTURED',
    'ORDER_CREATED',
    'DISPENSING_SLIP_GENERATED',
    'BRIDGE_SUPPLY_AUTHORIZED',
    'REFILL_SNOOZED',
    'CLINICAL_DDI_CHECK',
    'COMPLIANCE_ENGINE_INITIALIZED'
  ];

  constructor(private complianceService: ComplianceAndPaymentService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading = true;
    this.complianceService.getAuditLogs().subscribe({
      next: (data) => {
        this.logs = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onActionFilterChange(action: string): void {
    this.selectedAction = action;
    this.applyFilter();
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  applyFilter(): void {
    let result = this.logs;

    if (this.selectedAction !== 'ALL') {
      result = result.filter(log => log.action === this.selectedAction);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase().trim();
      result = result.filter(log =>
        (log.actorName && log.actorName.toLowerCase().includes(term)) ||
        (log.action && log.action.toLowerCase().includes(term)) ||
        (log.details && log.details.toLowerCase().includes(term)) ||
        (log.resourceType && log.resourceType.toLowerCase().includes(term))
      );
    }

    this.filteredLogs = result;
  }

  getActionBadgeClass(action: string): string {
    if (action.includes('APPROVAL') || action.includes('CAPTURED') || action.includes('INITIALIZED')) {
      return 'badge bg-success bg-opacity-10 text-success border border-success';
    }
    if (action.includes('BRIDGE') || action.includes('DDI')) {
      return 'badge bg-warning bg-opacity-10 text-warning text-dark border border-warning';
    }
    if (action.includes('REJECT') || action.includes('FAILED')) {
      return 'badge bg-danger bg-opacity-10 text-danger border border-danger';
    }
    return 'badge bg-primary bg-opacity-10 text-primary border border-primary';
  }
}
