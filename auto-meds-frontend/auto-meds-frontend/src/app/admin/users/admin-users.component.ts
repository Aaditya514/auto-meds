import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent implements OnInit {
  patients: User[] = [];
  loading = true;
  searchQuery = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.loading = true;
    this.adminService.getAllPatients().subscribe({
      next: (data) => {
        this.patients = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  get filteredPatients(): User[] {
    if (!this.searchQuery || !this.searchQuery.trim()) {
      return this.patients;
    }
    const q = this.searchQuery.toLowerCase().trim();
    return this.patients.filter(p => 
      p.name?.toLowerCase().includes(q) ||
      p.email?.toLowerCase().includes(q) ||
      p.phone?.toLowerCase().includes(q) ||
      p.city?.toLowerCase().includes(q) ||
      p.allergies?.toLowerCase().includes(q) ||
      p.chronicConditions?.toLowerCase().includes(q) ||
      String(p.id).includes(q)
    );
  }

  getInitial(name: string): string {
    return name ? name.charAt(0).toUpperCase() : 'P';
  }
}
