export interface Subscription {
  id: number;
  patientId: number;
  patientName?: string;
  patientEmail?: string;
  medicineId: number;
  medicineName: string;
  brandName: string;
  composition: string;
  strength: string;
  prescriptionId?: number;
  prescriptionFileName?: string;
  prescriptionExpiryDate?: string;
  dosage: string;
  frequency: string;
  quantity: number;
  startDate?: string;
  nextRefillDate?: string;
  nextDispatchDate?: string;
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'CANCELLED' | 'EXPIRED' | 'PRESCRIPTION_EXPIRED' | 'CLARIFICATION_REQUIRED' | 'PAUSED';
  createdAt?: string;
}
