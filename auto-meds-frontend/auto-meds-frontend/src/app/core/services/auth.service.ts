import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, throwError } from 'rxjs';
import { AuthResponse, User } from '../models/user.model';
import { API_BASE } from '../constants/api.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${API_BASE}/auth`;
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  public get token(): string | null {
    return this.currentUserValue ? this.currentUserValue.token : null;
  }

  public isLoggedIn(): boolean {
    return !!this.token;
  }

  public isAdmin(): boolean {
    return this.currentUserValue?.role === 'ADMIN';
  }

  public isPharmacist(): boolean {
    return this.isAdmin();
  }

  public isPatient(): boolean {
    return this.currentUserValue?.role === 'PATIENT';
  }

  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap(res => this.setSession(res))
    );
  }

  // Calls the ADMIN-protected endpoint; JWT token auto-attached by JwtInterceptor
  registerAdmin(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register-admin`, data);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => this.setSession(res))
    );
  }

  getProfile(): Observable<User> {
    return this.http.get<User>(`${API_BASE}/users/profile`);
  }

  updateProfile(data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${API_BASE}/users/profile`, data).pipe(
      tap(updatedUser => {
        const current = this.currentUserValue;
        if (current && updatedUser.name) {
          current.name = updatedUser.name;
          localStorage.setItem('currentUser', JSON.stringify(current));
          this.currentUserSubject.next(current);
        }
      })
    );
  }

  private setSession(authResponse: AuthResponse): void {
    localStorage.setItem('currentUser', JSON.stringify(authResponse));
    this.currentUserSubject.next(authResponse);
  }

  refreshToken(): Observable<AuthResponse> {
    const current = this.currentUserValue;
    if (!current?.refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken: current.refreshToken }).pipe(
      tap(res => this.setSession(res))
    );
  }

  logout(): void {
    const refreshToken = this.currentUserValue?.refreshToken;
    if (refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({
        error: () => {}
      });
    }
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }
}
