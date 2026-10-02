export interface Medicine {
  id: number;
  medicineName: string;
  brandName: string;
  composition: string;
  strength: string;
  category?: string;
  price: number;
  stockQuantity: number;
  reservedQuantity?: number;
  availableQuantity?: number;
  reorderThreshold?: number;
  suggestedReorderPackSize?: number;
  requiresPrescription: boolean;
  description?: string;
  manufacturer?: string;
  expiryDate?: string;
  active: boolean;
  inStock?: boolean;
  symptoms?: string;
  genericAlternativeId?: number;
  genericAlternativeName?: string;
  genericAlternativePrice?: number;
  genericSavingsText?: string;
}
