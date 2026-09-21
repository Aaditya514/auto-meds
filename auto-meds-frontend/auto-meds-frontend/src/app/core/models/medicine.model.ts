export interface Medicine {
  id: number;
  medicineName: string;
  brandName: string;
  composition: string;
  strength: string;
  category?: string;
  price: number;
  stockQuantity: number;
  requiresPrescription: boolean;
  description?: string;
  manufacturer?: string;
  expiryDate?: string;
  active: boolean;
  inStock?: boolean;
}
