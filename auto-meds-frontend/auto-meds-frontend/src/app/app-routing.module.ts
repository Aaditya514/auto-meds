import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { AdminLoginComponent } from './auth/admin-login/admin-login.component';
import { RegisterComponent } from './auth/register/register.component';
import { AdminRegisterComponent } from './auth/admin-register/admin-register.component';
import { HomeComponent } from './patient/home/home.component';
import { MedicinesComponent } from './patient/medicines/medicines.component';
import { CartComponent } from './patient/cart/cart.component';
import { CheckoutComponent } from './patient/checkout/checkout.component';
import { SubscriptionsComponent } from './patient/subscriptions/subscriptions.component';
import { SubscriptionCreateComponent } from './patient/subscriptions/subscription-create/subscription-create.component';
import { OrdersComponent } from './patient/orders/orders.component';
import { ProfileComponent } from './patient/profile/profile.component';

import { AdminDashboardComponent } from './admin/dashboard/admin-dashboard.component';
import { AdminMedicinesComponent } from './admin/medicines/admin-medicines.component';
import { AdminInventoryComponent } from './admin/inventory/admin-inventory.component';
import { AdminSubscriptionRequestsComponent } from './admin/subscriptions/admin-subscription-requests.component';
import { AdminOrdersComponent } from './admin/orders/admin-orders.component';
import { AdminUsersComponent } from './admin/users/admin-users.component';
import { AdminAuditLogsComponent } from './admin/audit-logs/admin-audit-logs.component';

import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  // Dedicated Separate Patient & Admin Authentication Portals
  { path: 'login/patient', component: LoginComponent },
  { path: 'login/admin', component: AdminLoginComponent },
  { path: 'login', redirectTo: 'login/patient', pathMatch: 'full' },
  { path: 'admin/login', redirectTo: 'login/admin', pathMatch: 'full' },
  { path: 'register', component: RegisterComponent },
  { path: 'register/patient', redirectTo: 'register', pathMatch: 'full' },
  { path: 'admin/register-admin', component: AdminRegisterComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  
  // Patient Routes
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'medicines', component: MedicinesComponent },
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [AuthGuard] },
  { path: 'subscriptions', component: SubscriptionsComponent, canActivate: [AuthGuard] },
  { path: 'subscriptions/create', component: SubscriptionCreateComponent, canActivate: [AuthGuard] },
  { path: 'subscriptions/:id', component: SubscriptionsComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },

  // Admin Routes
  { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  { path: 'admin/medicines', component: AdminMedicinesComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  { path: 'admin/inventory', component: AdminInventoryComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  { path: 'admin/subscription-requests', component: AdminSubscriptionRequestsComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  { path: 'admin/orders', component: AdminOrdersComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  { path: 'admin/users', component: AdminUsersComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },
  { path: 'admin/audit-logs', component: AdminAuditLogsComponent, canActivate: [AuthGuard, RoleGuard], data: { expectedRole: 'ADMIN' } },

  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
