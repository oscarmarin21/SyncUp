import api from './api';
import { Song } from '@/types/song.types';
import { User } from '@/types/user.types';
import { ApiResponse } from '@/types/api.types';

/**
 * Servicio para operaciones de administración.
 * Requerido según RF-010 a RF-014.
 */
export const adminService = {
  // ========== GESTIÓN DE CANCIONES (RF-010) ==========
  
  /**
   * Obtiene todas las canciones del catálogo.
   * 
   * @returns lista de canciones
   */
  async getSongs(): Promise<Song[]> {
    const response = await api.get<ApiResponse<Song[]>>('/admin/songs');
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
  
  /**
   * Crea una nueva canción.
   * Requerido según RF-010.
   * 
   * @param song datos de la canción
   * @returns canción creada
   */
  async createSong(song: Omit<Song, 'id'>): Promise<Song> {
    const response = await api.post<ApiResponse<Song>>('/admin/songs', song);
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al crear canción');
  },
  
  /**
   * Actualiza una canción existente.
   * Requerido según RF-010.
   * 
   * @param id identificador de la canción
   * @param song datos actualizados
   * @returns canción actualizada
   */
  async updateSong(id: number, song: Partial<Song>): Promise<Song> {
    const response = await api.put<ApiResponse<Song>>(`/admin/songs/${id}`, song);
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al actualizar canción');
  },
  
  /**
   * Elimina una canción.
   * Requerido según RF-010.
   * 
   * @param id identificador de la canción
   */
  async deleteSong(id: number): Promise<void> {
    const response = await api.delete<ApiResponse<null>>(`/admin/songs/${id}`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || 'Error al eliminar canción');
    }
  },

  /**
   * Sube un archivo de audio y devuelve la URL resultante.
   */
  async uploadSongAudio(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<ApiResponse<{ url: string }>>('/admin/songs/upload-audio', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (response.data.success && response.data.data?.url) {
      return response.data.data.url;
    }

    throw new Error(response.data.message || 'Error al subir audio');
  },
  
  /**
   * Carga canciones masivamente desde un archivo.
   * Requerido según RF-012.
   * 
   * @param file archivo de texto con canciones
   * @returns resultado de la carga (canciones cargadas, errores)
   */
  async bulkUpload(file: File): Promise<{ cancionesCargadas: number; errores: number; erroresDetalle: string[] }> {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post<ApiResponse<{
      cancionesCargadas: number;
      errores: number;
      erroresDetalle: string[];
    }>>('/admin/songs/bulk-upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    throw new Error(response.data.message || 'Error al cargar canciones');
  },
  
  // ========== GESTIÓN DE USUARIOS (RF-011) ==========
  
  /**
   * Obtiene todos los usuarios del sistema.
   * Requerido según RF-011.
   * 
   * @returns lista de usuarios
   */
  async getUsers(): Promise<User[]> {
    const response = await api.get<ApiResponse<User[]>>('/admin/users');
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
  
  /**
   * Elimina un usuario del sistema.
   * Requerido según RF-011.
   * 
   * @param username username del usuario a eliminar
   */
  async deleteUser(username: string): Promise<void> {
    const response = await api.delete<ApiResponse<null>>(`/admin/users/${username}`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || 'Error al eliminar usuario');
    }
  },
  
  // ========== MÉTRICAS (RF-013, RF-014) ==========
  
  /**
   * Obtiene métricas de géneros (para Pie Chart).
   * Requerido según RF-013 y RF-014.
   * 
   * @returns mapa de género -> cantidad de canciones
   */
  async getGenreMetrics(): Promise<Record<string, number>> {
    const response = await api.get<ApiResponse<Record<string, number>>>('/admin/metrics/genres');
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return {};
  },
  
  /**
   * Obtiene métricas de artistas más populares (para Bar Chart).
   * Requerido según RF-013 y RF-014.
   * 
   * @param top número de artistas top a retornar (opcional, por defecto 10)
   * @returns mapa de artista -> cantidad de canciones
   */
  async getArtistMetrics(top: number = 10): Promise<Record<string, number>> {
    const response = await api.get<ApiResponse<Record<string, number>>>(
      `/admin/metrics/artists?top=${top}`
    );
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return {};
  },
};

