import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './shared/components/navbar/navbar.component';

import { LoginComponent } from './auth/login/login.component';
import { AdminLoginComponent } from './auth/admin-login/admin-login.component';
import { RegisterComponent } from './auth/register/register.component';

import { HomeComponent } from './patient/home/home.component';
import { MedicinesComponent } from './patient/medicines/medicines.component';
import { CartComponent } from './patient/cart/cart.component';
import { CheckoutComponent } from './patient/checkout/checkout.component';
import { SubscriptionsComponent } from './patient/subscriptions/subscriptions.component';
import { SubscriptionCreateComponent } from './patient/subscriptions/subscription-create/subscription-create.component';
import { OrdersComponent } from './patient/orders/orders.component';

import { AdminDashboardComponent } from './admin/dashboard/admin-dashboard.component';
import { AdminMedicinesComponent } from './admin/medicines/admin-medicines.component';
import { AdminInventoryComponent } from './admin/inventory/admin-inventory.component';
import { AdminSubscriptionRequestsComponent } from './admin/subscriptions/admin-subscription-requests.component';
import { AdminOrdersComponent } from './admin/orders/admin-orders.component';
import { AdminUsersComponent } from './admin/users/admin-users.component';

import { JwtInterceptor } from './core/interceptors/jwt.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    LoginComponent,
    AdminLoginComponent,
    RegisterComponent,
    HomeComponent,
    MedicinesComponent,
    CartComponent,
    CheckoutComponent,
    SubscriptionsComponent,
    SubscriptionCreateComponent,
    OrdersComponent,
    AdminDashboardComponent,
    AdminMedicinesComponent,
    AdminInventoryComponent,
    AdminSubscriptionRequestsComponent,
    AdminOrdersComponent,
    AdminUsersComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
