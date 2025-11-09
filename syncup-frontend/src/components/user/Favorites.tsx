import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { userService } from '@/services/userService';
import { Song } from '@/types/song.types';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { SongCard } from './SongCard';
import { useAudioPlayer } from '@/context/AudioPlayerContext';

/**
 * Componente para gestionar favoritos del usuario.
 * Requerido según RF-002 y RF-009.
 */
export const Favorites = () => {
  const [favorites, setFavorites] = useState<Song[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { playSong, addToPlaylist } = useAudioPlayer();
  const location = useLocation();

  useEffect(() => {
    // Solo cargar si estamos en la ruta de favoritos
    if (location.pathname === '/user/favorites') {
      setIsLoading(true);
      loadFavorites();
    } else {
      // Si no estamos en la ruta, limpiar el estado inmediatamente
      setFavorites([]);
      setIsLoading(false);
      setError(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);
  
  // Eliminamos la adición automática de favoritos a la playlist
  // El usuario puede agregarlos manualmente cuando hace clic en reproducir

  const handleFavoriteChange = () => {
    // Recargar favoritos cuando cambian
    loadFavorites();
  };

  const loadFavorites = async () => {
    try {
      setIsLoading(true);
      const favs = await userService.getFavorites();
      setFavorites(favs);
    } catch (err: any) {
      setError(err.message || 'Error al cargar favoritos');
    } finally {
      setIsLoading(false);
    }
  };

  // Esta función ya no es necesaria porque SongCard maneja sus propios favoritos
  // Pero la mantenemos para compatibilidad si se necesita

  const handleExportCSV = async () => {
    try {
      const blob = await userService.exportFavoritesCSV();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `favoritos_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      setError(err.message || 'Error al exportar CSV');
    }
  };

  // Solo renderizar si estamos en la ruta correcta
  if (location.pathname !== '/user/favorites') {
    return null;
  }

  if (isLoading) {
    return <Loading />;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Mis Favoritos</h1>
        {favorites.length > 0 && (
          <button onClick={handleExportCSV} className="btn-primary">
            Exportar CSV
          </button>
        )}
      </div>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      {favorites.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No tienes canciones favoritas aún</p>
          <p className="text-gray-400 mt-2">Agrega canciones a favoritos desde la búsqueda</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {favorites.map((song) => (
            <SongCard
              key={song.id}
              song={song}
              onPlay={(song) => {
                addToPlaylist([song]);
                playSong(song);
              }}
              showFavoriteButton={true}
              onFavoriteChange={handleFavoriteChange}
            />
          ))}
        </div>
      )}
    </div>
  );
};

