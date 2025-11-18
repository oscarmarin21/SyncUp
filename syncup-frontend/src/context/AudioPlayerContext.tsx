import {
  createContext,
  useContext,
  useState,
  ReactNode,
  useCallback,
  useRef,
  useEffect,
} from 'react';
import { Song } from '@/types/song.types';
import { recommendationService } from '@/services/recommendationService';

interface AudioPlayerContextType {
  currentSong: Song | null;
  playlist: Song[];
  playSong: (song: Song) => void;
  addToPlaylist: (songs: Song[]) => void;
  clearPlaylist: () => void;
  nextSong: () => void;
  previousSong: () => void;
}

const AudioPlayerContext = createContext<AudioPlayerContextType | undefined>(undefined);

export const useAudioPlayer = () => {
  const context = useContext(AudioPlayerContext);
  if (!context) {
    throw new Error('useAudioPlayer must be used within an AudioPlayerProvider');
  }
  return context;
};

interface AudioPlayerProviderProps {
  children: ReactNode;
}

export const AudioPlayerProvider = ({ children }: AudioPlayerProviderProps) => {
  const [currentSong, setCurrentSong] = useState<Song | null>(null);
  const [playlist, setPlaylist] = useState<Song[]>([]);
  const [currentIndex, setCurrentIndex] = useState(-1); // ⬅️ ahora sí lo usamos

  // Refs para tener siempre el estado “actual” dentro de callbacks/efectos
  const currentSongRef = useRef<Song | null>(null);
  const playlistRef = useRef<Song[]>([]);
  const indexRef = useRef<number>(-1);

  // Sincronizar refs con state
  currentSongRef.current = currentSong;
  playlistRef.current = playlist;
  indexRef.current = currentIndex;

  const playSong = useCallback((song: Song) => {
    setPlaylist((prevPlaylist) => {
      // Buscar el índice en la playlist actual
      let index = prevPlaylist.findIndex((s) => s.id === song.id);

      // Si la canción no está en la playlist, agregarla al final
      if (index === -1) {
        const newPlaylist = [...prevPlaylist, song];
        setCurrentSong(song);
        setCurrentIndex(newPlaylist.length - 1);
        return newPlaylist;
      } else {
        // Si ya está, solo establecer como actual
        setCurrentSong(song);
        setCurrentIndex(index);
        return prevPlaylist;
      }
    });
  }, []);

  const addToPlaylist = useCallback((songs: Song[]) => {
    if (songs.length === 0) return;

    setPlaylist((prevPlaylist) => {
      // Filtrar canciones que no estén ya en la playlist
      const newSongs = songs.filter(
        (song) => !prevPlaylist.some((existing) => existing.id === song.id)
      );

      if (newSongs.length === 0) {
        return prevPlaylist; // No hay canciones nuevas, no actualizar
      }

      // Si no hay canción actual, usar la primera de las nuevas como actual
      if (!currentSongRef.current) {
        setCurrentSong(newSongs[0]);
        setCurrentIndex(prevPlaylist.length); // índice de esa primera nueva
      }

      return [...prevPlaylist, ...newSongs];
    });
  }, []);

  const clearPlaylist = useCallback(() => {
    setPlaylist([]);
    setCurrentSong(null);
    setCurrentIndex(-1);
  }, []);

  // ✅ NUEVO: nextSong simple y estable
  const nextSong = useCallback(() => {
    const list = playlistRef.current;
    if (list.length === 0) return;

    setCurrentIndex((prevIndex) => {
      if (prevIndex < 0) {
        // Si por alguna razón está en -1, saltamos al primero
        setCurrentSong(list[0]);
        return 0;
      }

      const nextIndex = Math.min(prevIndex + 1, list.length - 1);
      if (nextIndex !== prevIndex) {
        setCurrentSong(list[nextIndex]);
      }
      return nextIndex;
    });
  }, []);

  // ✅ NUEVO: previousSong simple y estable
  const previousSong = useCallback(() => {
    const list = playlistRef.current;
    if (list.length === 0) return;

    setCurrentIndex((prevIndex) => {
      if (prevIndex <= 0) {
        // Ya estamos en la primera
        setCurrentSong(list[0]);
        return 0;
      }

      const prevSongIndex = prevIndex - 1;
      setCurrentSong(list[prevSongIndex]);
      return prevSongIndex;
    });
  }, []);

  // ⭐ Lógica de: cuando cambia currentSong, precargar relacionadas si la cola está “vacía”
  useEffect(() => {
    const current = currentSongRef.current;
    if (!current) return;

    // Si YA hay una cola más grande que solo la actual, asumimos que viene de Radio u otra lógica
    if (playlistRef.current && playlistRef.current.length > 1) {
      return;
    }

    const loadRelated = async () => {
      try {
        const related = await recommendationService.startRadio(current.id, 20);

        // Quitar la misma canción actual
        const filtered = related.filter((s) => s.id !== current.id);

        if (filtered.length > 0) {
          addToPlaylist(filtered);
        }
      } catch (err) {
        console.error('Error cargando canciones relacionadas para la cola:', err);
      }
    };

    loadRelated();
  }, [currentSong?.id, addToPlaylist]);

  const value: AudioPlayerContextType = {
    currentSong,
    playlist,
    playSong,
    addToPlaylist,
    clearPlaylist,
    nextSong,
    previousSong,
  };

  return (
    <AudioPlayerContext.Provider value={value}>
      {children}
    </AudioPlayerContext.Provider>
  );
};
