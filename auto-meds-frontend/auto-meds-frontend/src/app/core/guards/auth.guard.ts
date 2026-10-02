import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.authService.isLoggedIn()) {
      return true;
    }

    if (state.url.startsWith('/admin')) {
      this.router.navigate(['/login/admin'], { queryParams: { returnUrl: state.url } });
    } else {
      this.router.navigate(['/login/patient'], { queryParams: { returnUrl: state.url } });
    }
    return false;
  }
}
