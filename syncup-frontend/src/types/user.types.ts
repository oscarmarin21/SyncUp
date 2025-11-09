export interface User {
  id: number;
  username: string;
  nombre: string;
  rol: 'USER' | 'ADMIN';
  password?: string; // Solo para requests, no debe venir del backend
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nombre: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  username: string;
  nombre: string;
  rol: string;
}

