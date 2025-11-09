import { useState, useEffect } from 'react';
import { Song } from '@/types/song.types';
import { userService } from '@/services/userService';
import { formatDuration } from '@/utils/helpers';

interface SongCardProps {
  song: Song;
  onPlay?: (song: Song) => void;
  showFavoriteButton?: boolean;
  index?: number;
  onFavoriteChange?: () => void; // Callback cuando cambia el estado de favorito
}

/**
 * Componente de tarjeta de canción con opciones para reproducir y agregar a favoritos.
 */
export const SongCard = ({ song, onPlay, showFavoriteButton = true, index, onFavoriteChange }: SongCardProps) => {
  const [isFavorite, setIsFavorite] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Verificar si la canción está en favoritos
  useEffect(() => {
    const checkFavorite = async () => {
      try {
        const favorites = await userService.getFavorites();
        setIsFavorite(favorites.some((f) => f.id === song.id));
      } catch (error) {
        console.error('Error al verificar favoritos:', error);
      }
    };

    if (showFavoriteButton) {
      checkFavorite();
    }
  }, [song.id, showFavoriteButton]);

  const handleToggleFavorite = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!showFavoriteButton) return;

    setIsLoading(true);
    try {
      if (isFavorite) {
        await userService.removeFavorite(song.id);
        setIsFavorite(false);
      } else {
        await userService.addFavorite(song.id);
        setIsFavorite(true);
      }
      
      // Notificar al componente padre si existe el callback
      if (onFavoriteChange) {
        onFavoriteChange();
      }
    } catch (error: any) {
      console.error('Error al cambiar favorito:', error);
      alert(error.message || 'Error al actualizar favoritos');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePlay = () => {
    if (onPlay) {
      onPlay(song);
    }
  };

  return (
    <div className="card hover:shadow-md transition-shadow relative group">
      {/* Botón de favorito */}
      {showFavoriteButton && (
        <button
          onClick={handleToggleFavorite}
          disabled={isLoading}
          className="absolute top-4 right-4 z-10 p-2 rounded-full hover:bg-red-50 transition-colors"
          title={isFavorite ? 'Eliminar de favoritos' : 'Agregar a favoritos'}
        >
          <svg
            className={`w-6 h-6 transition-colors ${
              isFavorite
                ? 'text-red-500 fill-current'
                : 'text-gray-400 hover:text-red-500'
            }`}
            fill={isFavorite ? 'currentColor' : 'none'}
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
            />
          </svg>
        </button>
      )}

      {/* Botón de reproducir */}
      <button
        onClick={handlePlay}
        className="absolute top-4 left-4 z-10 w-10 h-10 bg-primary-600 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity shadow-lg hover:bg-primary-700"
        title="Reproducir"
      >
        <svg className="w-5 h-5 ml-0.5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M6.3 2.841A1.5 1.5 0 004 4.11V15.89a1.5 1.5 0 002.3 1.269l9.344-5.89a1.5 1.5 0 000-2.538L6.3 2.84z" />
        </svg>
      </button>

      {/* Contenido */}
      <div className="pt-2">
        {index !== undefined && (
          <div className="flex items-center mb-2">
            <span className="text-primary-600 font-bold mr-2">#{index + 1}</span>
          </div>
        )}
        <h3 className="font-semibold text-lg mb-2 pr-16">{song.titulo}</h3>
        <p className="text-gray-600 mb-1">Artista: {song.artista}</p>
        <p className="text-gray-600 mb-1">Género: {song.genero}</p>
        <p className="text-gray-600 mb-1">Año: {song.año}</p>
        <p className="text-gray-600">Duración: {formatDuration(song.duracion)}</p>
      </div>
    </div>
  );
};

