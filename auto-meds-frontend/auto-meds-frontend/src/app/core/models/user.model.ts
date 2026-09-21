export interface User {
  id: number;
  name: string;
  email: string;
  role: 'PATIENT' | 'ADMIN';
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  pincode?: string;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  name: string;
  email: string;
  role: string;
}
