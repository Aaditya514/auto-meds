import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Medicine } from '../models/medicine.model';

@Injectable({
  providedIn: 'root'
})
export class MedicineService {
  private apiUrl = 'http://localhost:8080/api/medicines';

  constructor(private http: HttpClient) {}

  getMedicines(): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(this.apiUrl);
  }

  getMedicineById(id: number): Observable<Medicine> {
    return this.http.get<Medicine>(`${this.apiUrl}/${id}`);
  }

  searchMedicines(query: string): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(`${this.apiUrl}/search?query=${encodeURIComponent(query)}`);
  }

  getAlternativeMedicines(medicineId: number): Observable<Medicine[]> {
    return this.http.get<Medicine[]>(`${this.apiUrl}/${medicineId}/alternatives`);
  }
}
