export interface PrescriptionOcrCandidate {
  detectedText: string;
  medicineId?: number;
  medicineName?: string;
  brandName?: string;
  composition?: string;
  strength?: string;
  dosage?: string;
  frequency?: string;
  quantity?: number;
  price?: number;
  confidenceScore: number;
  confidenceBadge: 'HIGH_CONFIDENCE' | 'MEDIUM_CONFIDENCE' | 'LOW_CONFIDENCE';
  genericAlternative?: string;
  genericPrice?: number;
  savingsPercentage?: number;
}

export interface PrescriptionOcrResult {
  fileName: string;
  rawExtractedText?: string;
  doctorName?: string;
  doctorRegNumber?: string;
  clinicOrHospital?: string;
  prescriptionDate?: string;
  confidenceOverall: number;
  status: 'PROCESSED' | 'PARTIALLY_MATCHED' | 'MANUAL_REVIEW_REQUIRED';
  candidates: PrescriptionOcrCandidate[];
}
