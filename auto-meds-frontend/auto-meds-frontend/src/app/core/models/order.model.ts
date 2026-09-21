export interface OrderItem {
  id: number;
  medicineId: number;
  medicineName: string;
  brandName: string;
  composition: string;
  strength: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface Order {
  id: number;
  patientId: number;
  patientName?: string;
  patientEmail?: string;
  subscriptionId?: number;
  orderDate: string;
  totalAmount: number;
  deliveryAddress: string;
  paymentMethod: string;
  paymentStatus: string;
  orderStatus: 'PENDING' | 'APPROVED' | 'PACKED' | 'DISPATCHED' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED' | 'REJECTED';
  orderType: 'ONE_TIME' | 'SUBSCRIPTION_REFILL';
  expectedDeliveryDate?: string;
  items: OrderItem[];
}
