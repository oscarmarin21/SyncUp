import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { recommendationService } from '@/services/recommendationService';
import { songService } from '@/services/songService';
import { Song } from '@/types/song.types';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { useDebounce } from '@/hooks/useDebounce';
import { SongCard } from './SongCard';
import { useAudioPlayer } from '@/context/AudioPlayerContext';

/**
 * Componente para iniciar Radio desde una canción.
 * Requerido según RF-006.
 */
export const Radio = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState<Song[]>([]);
  const [selectedSong, setSelectedSong] = useState<Song | null>(null);
  const [radioSongs, setRadioSongs] = useState<Song[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingRadio, setIsLoadingRadio] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { playSong, addToPlaylist } = useAudioPlayer();
  const location = useLocation();

  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  
  // Limpiar estado cuando cambia la ruta
  useEffect(() => {
    setSearchTerm('');
    setSearchResults([]);
    setSelectedSong(null);
    setRadioSongs([]);
    setError(null);
  }, [location.pathname]);

  // Buscar canciones mientras se escribe
  useEffect(() => {
    const search = async () => {
      if (debouncedSearchTerm.trim().length > 0) {
        setIsLoading(true);
        try {
          const results = await songService.autocomplete(debouncedSearchTerm);
          setSearchResults(results.slice(0, 10)); // Limitar a 10 resultados
        } catch (err: any) {
          console.error('Error buscando canciones:', err);
        } finally {
          setIsLoading(false);
        }
      } else {
        setSearchResults([]);
      }
    };

    search();
  }, [debouncedSearchTerm]);

  const handleStartRadio = async () => {
    if (!selectedSong) {
      setError('Selecciona una canción para iniciar la radio');
      return;
    }

    try {
      setIsLoadingRadio(true);
      setError(null);
      const songs = await recommendationService.startRadio(selectedSong.id, 30);
      setRadioSongs(songs);
      // Agregar canciones de radio a la playlist
      if (songs.length > 0) {
        addToPlaylist(songs);
        // playSong ya agrega la canción a la playlist si no está
        playSong(songs[0]); // Reproducir la primera canción automáticamente
      }
    } catch (err: any) {
      setError(err.message || 'Error al iniciar radio');
    } finally {
      setIsLoadingRadio(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Radio</h1>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      <div className="card mb-6 max-w-2xl">
        <h2 className="text-xl font-semibold mb-4">Selecciona una canción para iniciar</h2>
        
        <div className="mb-4">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Busca una canción..."
            className="input-field w-full"
          />
        </div>

        {isLoading && <Loading />}

        {searchResults.length > 0 && (
          <div className="border border-gray-200 rounded-lg max-h-60 overflow-y-auto mb-4">
            {searchResults.map((song) => (
              <div
                key={song.id}
                onClick={() => {
                  setSelectedSong(song);
                  setSearchTerm(song.titulo);
                  setSearchResults([]);
                }}
                className={`p-3 cursor-pointer hover:bg-gray-50 ${
                  selectedSong?.id === song.id ? 'bg-primary-50 border-l-4 border-primary-600' : ''
                }`}
              >
                <p className="font-medium">{song.titulo}</p>
                <p className="text-sm text-gray-600">{song.artista}</p>
              </div>
            ))}
          </div>
        )}

        {selectedSong && (
          <div className="mb-4 p-4 bg-primary-50 rounded-lg">
            <p className="font-semibold">Canción seleccionada:</p>
            <p className="text-lg">{selectedSong.titulo} - {selectedSong.artista}</p>
          </div>
        )}

        <button
          onClick={handleStartRadio}
          disabled={!selectedSong || isLoadingRadio}
          className="btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoadingRadio ? 'Iniciando Radio...' : 'Iniciar Radio'}
        </button>
      </div>

      {radioSongs.length > 0 && (
        <div>
          <h2 className="text-2xl font-semibold mb-4">Cola de Reproducción ({radioSongs.length} canciones)</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {radioSongs.map((song, index) => (
              <SongCard
                key={song.id}
                song={song}
                onPlay={playSong}
                showFavoriteButton={true}
                index={index + 1}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

