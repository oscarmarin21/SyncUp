import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { recommendationService } from '@/services/recommendationService';
import { Song } from '@/types/song.types';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { SongCard } from './SongCard';
import { useAudioPlayer } from '@/context/AudioPlayerContext';

/**
 * Componente para Descubrimiento Semanal.
 * Requerido según RF-005.
 */
export const Recommendations = () => {
  const location = useLocation();
  const [recommendations, setRecommendations] = useState<Song[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { playSong, addToPlaylist } = useAudioPlayer();
  
  // Limpiar recomendaciones cuando cambia la ruta
  useEffect(() => {
    setRecommendations([]);
    setError(null);
  }, [location.pathname]);

  const handleGenerateDiscovery = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const songs = await recommendationService.getDiscoveryWeekly(20);
      setRecommendations(songs);
      // Agregar recomendaciones a la playlist
      if (songs.length > 0) {
        addToPlaylist(songs);
      }
    } catch (err: any) {
      setError(err.message || 'Error al generar descubrimiento semanal');
    } finally {
      setIsLoading(false);
    }
  };
  
  // Función para manejar cuando se quiere reproducir una canción
  const handlePlaySong = (song: Song) => {
    // Agregar a playlist si no está
    addToPlaylist([song]);
    // Reproducir inmediatamente
    playSong(song);
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Descubrimiento Semanal</h1>
        <button onClick={handleGenerateDiscovery} className="btn-primary" disabled={isLoading}>
          {isLoading ? 'Generando...' : 'Generar Playlist'}
        </button>
      </div>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      {isLoading && <Loading />}

      {recommendations.length === 0 && !isLoading ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">Presiona el botón para generar tu descubrimiento semanal</p>
          <p className="text-gray-400 mt-2">Basado en tus gustos musicales</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {recommendations.map((song, index) => (
            <SongCard
              key={song.id}
              song={song}
              onPlay={handlePlaySong}
              showFavoriteButton={true}
              index={index + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
};

