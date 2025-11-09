import api from './api';
import { User } from '@/types/user.types';
import { Song } from '@/types/song.types';
import { ApiResponse } from '@/types/api.types';

/**
 * Servicio para operaciones de usuario.
 * Requerido según RF-002 y RF-009.
 */
export const userService = {
  /**
   * Obtiene el perfil del usuario actual.
   * Requerido según RF-002.
   * 
   * @returns perfil del usuario
   */
  async getProfile(): Promise<User> {
    const response = await api.get<ApiResponse<User>>('/users/me');
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al obtener perfil');
  },
  
  /**
   * Actualiza el perfil del usuario actual.
   * Requerido según RF-002.
   * 
   * @param data datos a actualizar (nombre, password opcional)
   * @returns usuario actualizado
   */
  async updateProfile(data: Partial<User>): Promise<User> {
    const response = await api.put<ApiResponse<User>>('/users/me', data);
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al actualizar perfil');
  },
  
  /**
   * Obtiene la lista de canciones favoritas del usuario.
   * Requerido según RF-002.
   * 
   * @returns lista de canciones favoritas
   */
  async getFavorites(): Promise<Song[]> {
    const response = await api.get<ApiResponse<Song[]>>('/users/me/favorites');
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
  
  /**
   * Agrega una canción a los favoritos.
   * Requerido según RF-002.
   * 
   * @param songId identificador de la canción
   */
  async addFavorite(songId: number): Promise<void> {
    const response = await api.post<ApiResponse<null>>(`/users/me/favorites/${songId}`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || 'Error al agregar a favoritos');
    }
  },
  
  /**
   * Elimina una canción de los favoritos.
   * Requerido según RF-002.
   * 
   * @param songId identificador de la canción
   */
  async removeFavorite(songId: number): Promise<void> {
    const response = await api.delete<ApiResponse<null>>(`/users/me/favorites/${songId}`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || 'Error al eliminar de favoritos');
    }
  },
  
  /**
   * Descarga un reporte CSV de las canciones favoritas.
   * Requerido según RF-009.
   * 
   * @returns blob del archivo CSV
   */
  async exportFavoritesCSV(): Promise<Blob> {
    const response = await api.get('/users/me/favorites/export', {
      responseType: 'blob',
    });
    
    return response.data;
  },
};

