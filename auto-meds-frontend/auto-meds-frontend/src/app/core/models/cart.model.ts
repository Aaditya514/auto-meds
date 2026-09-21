export interface CartItem {
  id: number;
  medicineId: number;
  medicineName: string;
  brandName: string;
  composition: string;
  strength: string;
  price: number;
  quantity: number;
  subtotal: number;
  inStock: boolean;
}

export interface Cart {
  id: number;
  patientId: number;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  deliveryCharge: number;
  totalAmount: number;
}
