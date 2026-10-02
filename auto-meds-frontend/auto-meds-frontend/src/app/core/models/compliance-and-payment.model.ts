export interface DrugInteractionAlert {
  severity: 'CRITICAL' | 'HIGH' | 'MODERATE' | 'LOW';
  type: 'DRUG_DRUG_INTERACTION' | 'ALLERGY_CONFLICT' | 'CHRONIC_CONDITION_PRECAUTION';
  drug1: string;
  drug2?: string;
  alertMessage: string;
  clinicalRecommendation: string;
}

export interface PaymentRequest {
  orderId: number;
  paymentMethod: 'UPI' | 'CARD' | 'CASH_ON_DELIVERY';
  upiId?: string;
  cardNumber?: string;
  cardHolder?: string;
  expiryDate?: string;
  cvv?: string;
  otp?: string;
}

export interface PaymentResponse {
  success: boolean;
  transactionId: string;
  paymentMethod: string;
  amount: number;
  message: string;
  gatewayResponse?: string;
}

export interface DispensingSlipItem {
  medicineName: string;
  strength: string;
  batchNumber: string;
  expiryDate: string;
  quantity: number;
  instructions: string;
}

export interface DispensingSlip {
  orderId: number;
  orderDate: string;
  patientName: string;
  deliveryAddress: string;
  paymentMethod: string;
  paymentStatus: string;
  transactionId?: string;
  dispensingSlipCode: string;
  qrPayload: string;
  fourEyesApprovedBy: string;
  dispensedAt: string;
  items: DispensingSlipItem[];
  clinicalWarning?: string;
}

export interface AuditLog {
  id: number;
  actorId?: number;
  actorName: string;
  actorRole: string;
  action: string;
  resourceType: string;
  resourceId?: number;
  details: string;
  ipAddress?: string;
  createdAt: string;
}
