import api from './api';
import { User } from '@/types/user.types';
import { ApiResponse } from '@/types/api.types';

/**
 * Servicio para funcionalidades sociales.
 * Requerido según RF-007 y RF-008.
 */
export const socialService = {
  /**
   * Sigue a un usuario.
   * Requerido según RF-007.
   * 
   * @param username username del usuario a seguir
   */
  async followUser(username: string): Promise<void> {
    const response = await api.post<ApiResponse<null>>(`/users/${username}/follow`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || 'Error al seguir usuario');
    }
  },
  
  /**
   * Deja de seguir a un usuario.
   * Requerido según RF-007.
   * 
   * @param username username del usuario a dejar de seguir
   */
  async unfollowUser(username: string): Promise<void> {
    const response = await api.delete<ApiResponse<null>>(`/users/${username}/follow`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || 'Error al dejar de seguir usuario');
    }
  },
  
  /**
   * Obtiene sugerencias de usuarios a quienes seguir.
   * Requerido según RF-008.
   * 
   * @param maxSugerencias número máximo de sugerencias (opcional, por defecto 10)
   * @returns lista de usuarios sugeridos
   */
  async getSuggestions(maxSugerencias: number = 10): Promise<User[]> {
    const response = await api.get<ApiResponse<User[]>>(
      `/users/suggestions?maxSugerencias=${maxSugerencias}`
    );
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },

  /**
   * Obtiene la lista de usuarios que el usuario actual sigue.
   */
  async getFollowing(): Promise<User[]> {
    const response = await api.get<ApiResponse<User[]>>('/users/me/following');

    if (response.data.success && response.data.data) {
      return response.data.data;
    }

    return [];
  },

  /**
   * Obtiene la lista de seguidores del usuario actual.
   */
  async getFollowers(): Promise<User[]> {
    const response = await api.get<ApiResponse<User[]>>('/users/me/followers');

    if (response.data.success && response.data.data) {
      return response.data.data;
    }

    return [];
  },
};

