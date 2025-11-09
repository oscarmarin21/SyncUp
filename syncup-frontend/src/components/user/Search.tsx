import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { songService } from '@/services/songService';
import { Song, SearchRequest } from '@/types/song.types';
import { useDebounce } from '@/hooks/useDebounce';
import { Loading } from '@/components/common/Loading';
import { SongCard } from './SongCard';
import { useAudioPlayer } from '@/context/AudioPlayerContext';

/**
 * Componente de búsqueda con autocompletado y búsqueda avanzada.
 * Requerido según RF-003 y RF-004.
 */
export const Search = () => {
  const location = useLocation();
  const [searchTerm, setSearchTerm] = useState('');
  const [autocompleteResults, setAutocompleteResults] = useState<Song[]>([]);
  const [isAdvancedSearch, setIsAdvancedSearch] = useState(false);
  const [advancedResults, setAdvancedResults] = useState<Song[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { playSong, addToPlaylist } = useAudioPlayer();
  
  const [advancedForm, setAdvancedForm] = useState<SearchRequest>({
    artista: '',
    genero: '',
    operador: 'AND',
  });

  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  
  // Limpiar resultados cuando cambia la ruta
  useEffect(() => {
    setAutocompleteResults([]);
    setAdvancedResults([]);
    setSearchTerm('');
  }, [location.pathname]);

  // Autocompletado cuando cambia el término de búsqueda
  useEffect(() => {
    const fetchAutocomplete = async () => {
      if (debouncedSearchTerm.trim().length > 0) {
        setIsLoading(true);
        try {
          const results = await songService.autocomplete(debouncedSearchTerm);
          setAutocompleteResults(results);
        } catch (error) {
          console.error('Error en autocompletado:', error);
          setAutocompleteResults([]);
        } finally {
          setIsLoading(false);
        }
      } else {
        setAutocompleteResults([]);
      }
    };

    fetchAutocomplete();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearchTerm]);

  const handleAdvancedSearch = async () => {
    setIsLoading(true);
    try {
      const results = await songService.advancedSearch(advancedForm);
      setAdvancedResults(results);
    } catch (error) {
      console.error('Error en búsqueda avanzada:', error);
      setAdvancedResults([]);
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

  const results = isAdvancedSearch ? advancedResults : autocompleteResults;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Buscar Canciones</h1>
      
      <div className="mb-4">
        <button
          onClick={() => setIsAdvancedSearch(!isAdvancedSearch)}
          className="btn-secondary"
        >
          {isAdvancedSearch ? 'Búsqueda Simple' : 'Búsqueda Avanzada'}
        </button>
      </div>

      {!isAdvancedSearch ? (
        <div className="mb-6">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Busca canciones por título..."
            className="input-field w-full max-w-md"
          />
          {isLoading && <Loading />}
        </div>
      ) : (
        <div className="card mb-6 max-w-2xl">
          <h2 className="text-xl font-semibold mb-4">Búsqueda Avanzada</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Artista
              </label>
              <input
                type="text"
                value={advancedForm.artista || ''}
                onChange={(e) => setAdvancedForm({ ...advancedForm, artista: e.target.value })}
                className="input-field"
                placeholder="Nombre del artista"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Género
              </label>
              <input
                type="text"
                value={advancedForm.genero || ''}
                onChange={(e) => setAdvancedForm({ ...advancedForm, genero: e.target.value })}
                className="input-field"
                placeholder="Género musical"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Año
              </label>
              <input
                type="number"
                value={advancedForm.año || ''}
                onChange={(e) => setAdvancedForm({ 
                  ...advancedForm, 
                  año: e.target.value ? parseInt(e.target.value) : undefined 
                })}
                className="input-field"
                placeholder="Año de lanzamiento"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Operador Lógico
              </label>
              <select
                value={advancedForm.operador || 'AND'}
                onChange={(e) => setAdvancedForm({ 
                  ...advancedForm, 
                  operador: e.target.value as 'AND' | 'OR' 
                })}
                className="input-field"
              >
                <option value="AND">AND (todas las condiciones)</option>
                <option value="OR">OR (cualquier condición)</option>
              </select>
            </div>
            
            <button onClick={handleAdvancedSearch} className="btn-primary" disabled={isLoading}>
              {isLoading ? 'Buscando...' : 'Buscar'}
            </button>
          </div>
        </div>
      )}

      <div className="mt-6">
        <h2 className="text-xl font-semibold mb-4">
          Resultados ({results.length})
        </h2>
        
        {results.length === 0 && !isLoading && (
          <p className="text-gray-500 text-center py-8">
            No se encontraron canciones
          </p>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {results.map((song) => (
            <SongCard
              key={song.id}
              song={song}
              onPlay={handlePlaySong}
              showFavoriteButton={true}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

