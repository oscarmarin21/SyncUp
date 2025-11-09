import api from './api';
import { Song } from '@/types/song.types';
import { ApiResponse } from '@/types/api.types';

/**
 * Servicio para recomendaciones musicales.
 * Requerido según RF-005 y RF-006.
 */
export const recommendationService = {
  /**
   * Genera una playlist de "Descubrimiento Semanal".
   * Requerido según RF-005.
   * 
   * @param maxCanciones número máximo de canciones (opcional, por defecto 20)
   * @returns lista de canciones recomendadas
   */
  async getDiscoveryWeekly(maxCanciones: number = 20): Promise<Song[]> {
    const response = await api.get<ApiResponse<Song[]>>(
      `/recommendations/discovery-weekly?maxCanciones=${maxCanciones}`
    );
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
  
  /**
   * Inicia una "Radio" a partir de una canción semilla.
   * Requerido según RF-006.
   * 
   * @param songId identificador de la canción semilla
   * @param maxCanciones número máximo de canciones (opcional, por defecto 30)
   * @returns lista de canciones para la radio
   */
  async startRadio(songId: number, maxCanciones: number = 30): Promise<Song[]> {
    const response = await api.post<ApiResponse<Song[]>>(
      `/recommendations/radio?songId=${songId}&maxCanciones=${maxCanciones}`
    );
    
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    
    return [];
  },
};

