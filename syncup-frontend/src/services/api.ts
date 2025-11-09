import axios, { AxiosInstance, AxiosError } from 'axios';
import { API_BASE_URL, TOKEN_KEY } from '@/utils/constants';
import { ApiError } from '@/types/api.types';

/**
 * Configuración de Axios para todas las requests HTTP.
 * Maneja JWT tokens y errores de forma centralizada.
 */
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar token JWT a todas las requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar errores globalmente
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem('syncup_user');
      window.location.href = '/login';
    }
    
    // Retornar error formateado
    const apiError: ApiError = {
      message: error.response?.data?.message || error.message || 'Error de conexión',
      errors: error.response?.data?.errors,
    };
    
    return Promise.reject(apiError);
  }
);

export default api;

