import { createContext, useContext, useState, ReactNode, useCallback, useRef } from 'react';
import { Song } from '@/types/song.types';

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
  const [, setCurrentIndex] = useState(-1);
  const currentSongRef = useRef<Song | null>(null);
  const playlistRef = useRef<Song[]>([]);

  // Sincronizar refs con state
  currentSongRef.current = currentSong;
  playlistRef.current = playlist;

  const playSong = useCallback((song: Song) => {
    setPlaylist((prevPlaylist) => {
      // Buscar el índice en la playlist actual
      let index = prevPlaylist.findIndex((s) => s.id === song.id);
      
      // Si la canción no está en la playlist, agregarla
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
      
      // Actualizar currentSong solo si no hay canción actual
      if (!currentSongRef.current) {
        setCurrentSong(newSongs[0]);
        setCurrentIndex(prevPlaylist.length);
      }
      
      return [...prevPlaylist, ...newSongs];
    });
  }, []);

  const clearPlaylist = useCallback(() => {
    setPlaylist([]);
    setCurrentSong(null);
    setCurrentIndex(-1);
  }, []);

  const nextSong = useCallback(() => {
    setPlaylist((prevPlaylist) => {
      if (prevPlaylist.length === 0) return prevPlaylist;
      
      setCurrentIndex((prevIndex) => {
        if (prevIndex < prevPlaylist.length - 1) {
          const nextIndex = prevIndex + 1;
          setCurrentSong(prevPlaylist[nextIndex]);
          return nextIndex;
        }
        return prevIndex;
      });
      return prevPlaylist;
    });
  }, []);

  const previousSong = useCallback(() => {
    setPlaylist((prevPlaylist) => {
      if (prevPlaylist.length === 0) return prevPlaylist;
      
      setCurrentIndex((prevIndex) => {
        if (prevIndex > 0) {
          const prevSongIndex = prevIndex - 1;
          setCurrentSong(prevPlaylist[prevSongIndex]);
          return prevSongIndex;
        }
        return prevIndex;
      });
      return prevPlaylist;
    });
  }, []);

  const value: AudioPlayerContextType = {
    currentSong,
    playlist,
    playSong,
    addToPlaylist,
    clearPlaylist,
    nextSong,
    previousSong,
  };

  return <AudioPlayerContext.Provider value={value}>{children}</AudioPlayerContext.Provider>;
};

