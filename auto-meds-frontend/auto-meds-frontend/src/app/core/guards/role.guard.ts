import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const expectedRole = route.data['expectedRole'];
    const currentUser = this.authService.currentUserValue;

    if (!currentUser || currentUser.role !== expectedRole) {
      if (currentUser?.role === 'PATIENT') {
        this.router.navigate(['/home']);
      } else if (currentUser?.role === 'ADMIN') {
        this.router.navigate(['/admin/dashboard']);
      } else {
        this.router.navigate(['/login']);
      }
      return false;
    }

    return true;
  }
}
