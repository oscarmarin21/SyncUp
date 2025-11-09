import api from './api';
import { Song, SearchRequest } from '@/types/song.types';
import { ApiResponse } from '@/types/api.types';

/**
 * Servicio para operaciones relacionadas con canciones.
 */
export const songService = {
  /**
   * Busca canciones por autocompletado de título.
   * Requerido según RF-003.
   * 
   * @param prefix prefijo de búsqueda
   * @returns lista de canciones que coinciden con el prefijo
   */
  async autocomplete(prefix: string): Promise<Song[]> {
    if (!prefix || prefix.trim().length === 0) {
      return [];
    }
    
    const response = await api.get<ApiResponse<Song[]>>(`/songs/autocomplete?prefix=${encodeURIComponent(prefix)}`);
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
  
  /**
   * Realiza una búsqueda avanzada de canciones.
   * Requerido según RF-004.
   * 
   * @param searchRequest criterios de búsqueda
   * @returns lista de canciones que coinciden con los criterios
   */
  async advancedSearch(searchRequest: SearchRequest): Promise<Song[]> {
    const response = await api.post<ApiResponse<Song[]>>('/songs/search/advanced', searchRequest);
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
  
  /**
   * Obtiene una canción por su ID.
   * 
   * @param id identificador de la canción
   * @returns canción encontrada
   */
  async getSongById(id: number): Promise<Song | null> {
    const response = await api.get<ApiResponse<Song>>(`/songs/${id}`);
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return null;
  },
};

