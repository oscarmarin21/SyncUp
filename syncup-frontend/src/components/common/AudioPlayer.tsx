import { useState, useEffect, useRef, ChangeEvent } from 'react';
import { Song } from '@/types/song.types';
import { formatDuration } from '@/utils/helpers';
import { MEDIA_BASE_URL } from '@/utils/constants';

interface AudioPlayerProps {
  song: Song | null;
  onNext?: () => void;
  onPrevious?: () => void;
}

const resolveAudioUrl = (song: Song | null): string | null => {
  if (!song || !song.audioUrl) {
    return null;
  }

  if (song.audioUrl.startsWith('http://') || song.audioUrl.startsWith('https://')) {
    return song.audioUrl;
  }

  if (song.audioUrl.startsWith('/')) {
    return `${MEDIA_BASE_URL}${song.audioUrl}`;
  }

  return `${MEDIA_BASE_URL}/${song.audioUrl}`;
};

/**
 * Componente reproductor de audio que utiliza un elemento HTMLAudioElement real.
 */
export const AudioPlayer = ({ song, onNext, onPrevious }: AudioPlayerProps) => {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(song?.duracion ?? 0);
  const [volume, setVolume] = useState(1);

  // Sincronizar volumen con el elemento de audio
  useEffect(() => {
    const audio = audioRef.current;
    if (audio) {
      audio.volume = volume;
    }
  }, [volume]);

  // Configurar listeners para el elemento de audio
  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) {
      return;
    }

    const handleTimeUpdate = () => setCurrentTime(audio.currentTime);
    const handleLoadedMetadata = () => {
      if (!Number.isNaN(audio.duration) && audio.duration > 0) {
        setDuration(audio.duration);
      } else if (song?.duracion) {
        setDuration(song.duracion);
      }
    };
    const handleEnded = () => {
      setIsPlaying(false);
      if (onNext) {
        onNext();
      }
    };
    const handlePlay = () => setIsPlaying(true);
    const handlePause = () => setIsPlaying(false);

    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('loadedmetadata', handleLoadedMetadata);
    audio.addEventListener('ended', handleEnded);
    audio.addEventListener('play', handlePlay);
    audio.addEventListener('pause', handlePause);

    return () => {
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
      audio.removeEventListener('ended', handleEnded);
      audio.removeEventListener('play', handlePlay);
      audio.removeEventListener('pause', handlePause);
    };
  }, [onNext, song]);

  // Manejar cambio de canción
  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) {
      return;
    }

    const resolvedUrl = resolveAudioUrl(song);

    if (!song || !resolvedUrl) {
      audio.pause();
      audio.removeAttribute('src');
      audio.load();
      setIsPlaying(false);
      setCurrentTime(0);
      setDuration(song?.duracion ?? 0);
      return;
    }

    if (audio.src !== resolvedUrl) {
      audio.src = resolvedUrl;
      audio.load();
    }

    audio.currentTime = 0;
    setCurrentTime(0);
    setDuration(song.duracion ?? 0);

    const playPromise = audio.play();
    if (playPromise !== undefined) {
      playPromise
        .then(() => setIsPlaying(true))
        .catch((error) => {
          console.warn('No se pudo reproducir automáticamente el audio:', error);
          setIsPlaying(false);
        });
    }
  }, [song]);

  // Limpiar al desmontar
  useEffect(() => {
    return () => {
      const audio = audioRef.current;
      if (audio) {
        audio.pause();
      }
    };
  }, []);

  const handlePlayPause = () => {
    const audio = audioRef.current;
    if (!audio) return;

    if (isPlaying) {
      audio.pause();
    } else {
      const playPromise = audio.play();
      if (playPromise !== undefined) {
        playPromise.catch((error) => {
          console.warn('Error al intentar reproducir:', error);
        });
      }
    }
  };

  const handleSeek = (e: ChangeEvent<HTMLInputElement>) => {
    const audio = audioRef.current;
    if (!audio) return;

    const newTime = parseFloat(e.target.value);
    audio.currentTime = newTime;
    setCurrentTime(newTime);
  };

  const handleVolumeChange = (e: ChangeEvent<HTMLInputElement>) => {
    const newVolume = parseFloat(e.target.value);
    setVolume(newVolume);
  };

  if (!song) {
    return null;
  }

  const progressPercentage = duration > 0 ? (currentTime / duration) * 100 : 0;
  const hasAudio = Boolean(resolveAudioUrl(song));

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg z-50">
      <audio ref={audioRef} hidden />
      <div className="container mx-auto px-4 py-3">
        <div className="flex items-center justify-between">
          {/* Info de la canción */}
          <div className="flex items-center space-x-4 flex-1 min-w-0">
            <div className="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-primary-500 to-primary-700 rounded flex items-center justify-center text-white font-bold">
              {song.titulo.charAt(0)}
            </div>
            <div className="min-w-0 flex-1">
              <p className="font-semibold truncate">{song.titulo}</p>
              <p className="text-sm text-gray-600 truncate">{song.artista}</p>
              {!hasAudio && (
                <p className="text-xs text-red-500 mt-1">Audio no disponible para esta pista.</p>
              )}
            </div>
          </div>

          {/* Controles principales */}
          <div className="flex items-center space-x-4 flex-1 justify-center">
            {onPrevious && (
              <button
                onClick={onPrevious}
                className="text-gray-600 hover:text-primary-600 transition-colors disabled:opacity-40"
                title="Anterior"
                disabled={!hasAudio}
              >
                <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M8.445 14.832A1 1 0 0010 14v-2.798l5.445 3.63A1 1 0 0017 15V5a1 1 0 00-1.555-.832L10 7.798V5a1 1 0 00-1.555-.832l-6 4a1 1 0 000 1.664l6 4z" />
                </svg>
              </button>
            )}
            
            <button
              onClick={handlePlayPause}
              className="w-10 h-10 bg-primary-600 text-white rounded-full flex items-center justify-center hover:bg-primary-700 transition-colors disabled:opacity-40"
              title={isPlaying ? 'Pausar' : 'Reproducir'}
              disabled={!hasAudio}
            >
              {isPlaying ? (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg className="w-5 h-5 ml-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M6.3 2.841A1.5 1.5 0 004 4.11V15.89a1.5 1.5 0 002.3 1.269l9.344-5.89a1.5 1.5 0 000-2.538L6.3 2.84z" />
                </svg>
              )}
            </button>

            {onNext && (
              <button
                onClick={onNext}
                className="text-gray-600 hover:text-primary-600 transition-colors disabled:opacity-40"
                title="Siguiente"
                disabled={!hasAudio}
              >
                <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M4.555 5.168A1 1 0 003 6v8a1 1 0 001.555.832L10 11.202V14a1 1 0 001.555.832l6-4a1 1 0 000-1.664l-6-4A1 1 0 0011 6v2.798l-5.445-3.63z" />
                </svg>
              </button>
            )}

            <div className="flex items-center space-x-2 ml-4">
              <span className="text-sm text-gray-600">{formatDuration(currentTime)}</span>
              <input
                type="range"
                min="0"
                max={duration || song.duracion}
                value={currentTime}
                onChange={handleSeek}
                className="w-32"
                disabled={!hasAudio}
              />
              <span className="text-sm text-gray-600">{formatDuration(duration || song.duracion)}</span>
            </div>
          </div>

          {/* Volumen */}
          <div className="flex items-center space-x-2 flex-1 justify-end">
            <svg className="w-5 h-5 text-gray-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M9.383 3.076A1 1 0 0110 4v12a1 1 0 01-1.617.793L4.383 13H2a1 1 0 01-1-1V8a1 1 0 011-1h2.383l4.617-3.793a1 1 0 011.383.07zM14.657 2.929a1 1 0 011.414 0A9.972 9.972 0 0119 10a9.972 9.972 0 01-2.929 7.071 1 1 0 01-1.414-1.414A7.971 7.971 0 0017 10c0-2.21-.894-4.208-2.343-5.657a1 1 0 010-1.414zm-2.829 2.828a1 1 0 011.415 0A5.983 5.983 0 0115 10a5.984 5.984 0 01-1.757 4.243 1 1 0 01-1.415-1.415A3.984 3.984 0 0013 10a3.983 3.983 0 00-1.172-2.828 1 1 0 010-1.415z" clipRule="evenodd" />
            </svg>
            <input
              type="range"
              min="0"
              max="1"
              step="0.1"
              value={volume}
              onChange={handleVolumeChange}
              className="w-24"
              disabled={!hasAudio}
            />
          </div>
        </div>

        {/* Barra de progreso */}
        <div className="mt-2">
          <div className="h-1 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-primary-600 transition-all duration-300"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

