import api from './api';
import { LoginRequest, RegisterRequest, JwtResponse } from '@/types/user.types';
import { ApiResponse } from '@/types/api.types';
import { TOKEN_KEY, USER_KEY } from '@/utils/constants';

/**
 * Servicio de autenticación.
 * Maneja login, registro y logout.
 */
export const authService = {
  /**
   * Inicia sesión en el sistema.
   * 
   * @param credentials credenciales de login
   * @returns respuesta con token JWT
   */
  async login(credentials: LoginRequest): Promise<JwtResponse> {
    const response = await api.post<ApiResponse<JwtResponse>>('/auth/login', credentials);
    
    if (response.data.success && response.data.data) {
      const jwtResponse = response.data.data;
      
      // Guardar token y usuario en localStorage
      localStorage.setItem(TOKEN_KEY, jwtResponse.token);
      localStorage.setItem(USER_KEY, JSON.stringify({
        username: jwtResponse.username,
        nombre: jwtResponse.nombre,
        rol: jwtResponse.rol,
      }));
      
      return jwtResponse;
    }
    
    throw new Error(response.data.message || 'Error al iniciar sesión');
  },

  /**
   * Actualiza los datos del usuario almacenado en localStorage.
   *
   * @param user datos a persistir
   */
  setUser(user: { username: string; nombre: string; rol: string } | null): void {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  },
  
  /**
   * Registra un nuevo usuario en el sistema.
   * 
   * @param userData datos de registro
   * @returns respuesta con token JWT (auto-login)
   */
  async register(userData: RegisterRequest): Promise<JwtResponse> {
    const response = await api.post<ApiResponse<JwtResponse>>('/auth/register', userData);
    
    if (response.data.success && response.data.data) {
      const jwtResponse = response.data.data;
      
      // Guardar token y usuario en localStorage
      localStorage.setItem(TOKEN_KEY, jwtResponse.token);
      localStorage.setItem(USER_KEY, JSON.stringify({
        username: jwtResponse.username,
        nombre: jwtResponse.nombre,
        rol: jwtResponse.rol,
      }));
      
      return jwtResponse;
    }
    
    throw new Error(response.data.message || 'Error al registrar usuario');
  },
  
  /**
   * Cierra sesión y limpia el almacenamiento local.
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = '/login';
  },
  
  /**
   * Obtiene el token almacenado.
   * 
   * @returns token JWT o null
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },
  
  /**
   * Obtiene el usuario almacenado.
   * 
   * @returns datos del usuario o null
   */
  getUser(): { username: string; nombre: string; rol: string } | null {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },
  
  /**
   * Verifica si hay un usuario autenticado.
   * 
   * @returns true si hay token y usuario, false en caso contrario
   */
  isAuthenticated(): boolean {
    return !!(this.getToken() && this.getUser());
  },
};

