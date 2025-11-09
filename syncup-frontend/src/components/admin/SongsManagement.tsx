import { useState, useEffect, ChangeEvent } from 'react';
import { useForm } from 'react-hook-form';
import { adminService } from '@/services/adminService';
import { Song } from '@/types/song.types';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { formatDuration } from '@/utils/helpers';
import { MEDIA_BASE_URL } from '@/utils/constants';

type SongFormValues = Omit<Song, 'id'>;

/**
 * Componente para gestión de canciones (CRUD).
 * Requerido según RF-010.
 */
export const SongsManagement = () => {
  const [songs, setSongs] = useState<Song[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSong, setEditingSong] = useState<Song | null>(null);
  const [isUploadingAudio, setIsUploadingAudio] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<SongFormValues>({
    defaultValues: {
      audioUrl: '',
    },
  });

  const audioUrlValue = watch('audioUrl');

  useEffect(() => {
    loadSongs();
  }, []);

  const loadSongs = async () => {
    try {
      setIsLoading(true);
      const data = await adminService.getSongs();
      setSongs(data);
    } catch (err: any) {
      setError(err.message || 'Error al cargar canciones');
    } finally {
      setIsLoading(false);
    }
  };

  const buildSongPayload = (data: SongFormValues): SongFormValues => ({
    titulo: data.titulo,
    artista: data.artista,
    genero: data.genero,
    año: data.año,
    duracion: data.duracion,
    audioUrl: data.audioUrl?.trim() ? data.audioUrl.trim() : undefined,
  });

  const handleCreate = async (data: SongFormValues) => {
    try {
      await adminService.createSong(buildSongPayload(data));
      setIsModalOpen(false);
      reset();
      setValue('audioUrl', '');
      setUploadError(null);
      loadSongs();
    } catch (err: any) {
      setError(err.message || 'Error al crear canción');
    }
  };

  const handleEdit = (song: Song) => {
    setEditingSong(song);
    reset({
      titulo: song.titulo,
      artista: song.artista,
      genero: song.genero,
      año: song.año,
      duracion: song.duracion,
      audioUrl: song.audioUrl ?? '',
    });
    setUploadError(null);
    setValue('audioUrl', song.audioUrl ?? '');
    setIsModalOpen(true);
  };

  const handleUpdate = async (data: SongFormValues) => {
    if (!editingSong) return;

    try {
      await adminService.updateSong(editingSong.id, buildSongPayload(data));
      setIsModalOpen(false);
      setEditingSong(null);
      reset();
      setValue('audioUrl', '');
      setUploadError(null);
      loadSongs();
    } catch (err: any) {
      setError(err.message || 'Error al actualizar canción');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Estás seguro de eliminar esta canción?')) return;

    try {
      await adminService.deleteSong(id);
      loadSongs();
    } catch (err: any) {
      setError(err.message || 'Error al eliminar canción');
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingSong(null);
    reset();
    setValue('audioUrl', '');
    setUploadError(null);
  };

  const resolveAudioUrl = (audioUrl?: string) => {
    if (!audioUrl) return '';
    if (audioUrl.startsWith('http://') || audioUrl.startsWith('https://')) {
      return audioUrl;
    }
    if (audioUrl.startsWith('/')) {
      return `${MEDIA_BASE_URL}${audioUrl}`;
    }
    return `${MEDIA_BASE_URL}/${audioUrl}`;
  };

  const handleAudioUpload = async (event: ChangeEvent<HTMLInputElement>) => {
    const input = event.target;
    const file = input.files?.[0];
    if (!file) return;

    setUploadError(null);
    setIsUploadingAudio(true);

    try {
      // Obtener duración del audio antes de subirlo
      const duration = await obtenerDuracionAudio(file);
      if (!Number.isNaN(duration) && Number.isFinite(duration) && duration > 0) {
        const rounded = Math.max(1, Math.round(duration));
        setValue('duracion', rounded, { shouldValidate: true, shouldDirty: true });
      }

      const url = await adminService.uploadSongAudio(file);
      setValue('audioUrl', url, { shouldValidate: true });
    } catch (err: any) {
      console.error('Error subiendo audio:', err);
      setUploadError(err.message || 'Error al subir el audio');
    } finally {
      setIsUploadingAudio(false);
      // limpiar input para permitir volver a cargar el mismo archivo
      input.value = '';
    }
  };

  const handleRemoveAudio = () => {
    setValue('audioUrl', '');
    setUploadError(null);
  };

  const obtenerDuracionAudio = (file: File): Promise<number> => {
    return new Promise((resolve, reject) => {
      try {
        const audio = document.createElement('audio');
        const objectUrl = URL.createObjectURL(file);
        audio.preload = 'metadata';
        audio.src = objectUrl;

        const limpiar = () => {
          audio.remove();
          URL.revokeObjectURL(objectUrl);
        };

        audio.onloadedmetadata = () => {
          const duracion = audio.duration;
          limpiar();
          resolve(duracion);
        };

        audio.onerror = (e) => {
          console.error('No se pudo leer la duración del audio', e);
          limpiar();
          reject(new Error('No se pudo obtener la duración del audio. Ingresa el valor manualmente.'));
        };
      } catch (error) {
        reject(error);
      }
    });
  };

  if (isLoading) {
    return <Loading />;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Gestión de Canciones</h1>
        <button
          onClick={() => {
            setEditingSong(null);
            reset();
            setValue('audioUrl', '');
            setUploadError(null);
            setIsModalOpen(true);
          }}
          className="btn-primary"
        >
          + Nueva Canción
        </button>
      </div>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      {/* Modal para crear/editar */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h2 className="text-xl font-semibold mb-4">
              {editingSong ? 'Editar Canción' : 'Nueva Canción'}
            </h2>
            
            <form onSubmit={handleSubmit(editingSong ? handleUpdate : handleCreate)} className="space-y-4">
              <input type="hidden" {...register('audioUrl')} />

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Título</label>
                <input {...register('titulo', { required: 'Título requerido' })} className="input-field" />
                {errors.titulo && <p className="text-red-600 text-sm mt-1">{errors.titulo.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Artista</label>
                <input {...register('artista', { required: 'Artista requerido' })} className="input-field" />
                {errors.artista && <p className="text-red-600 text-sm mt-1">{errors.artista.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Género</label>
                <input {...register('genero', { required: 'Género requerido' })} className="input-field" />
                {errors.genero && <p className="text-red-600 text-sm mt-1">{errors.genero.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Año</label>
                <input 
                  type="number" 
                  {...register('año', { required: 'Año requerido', min: 1900, valueAsNumber: true })} 
                  className="input-field" 
                />
                {errors.año && <p className="text-red-600 text-sm mt-1">{errors.año.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Duración (segundos)</label>
                <input 
                  type="number" 
                  {...register('duracion', { required: 'Duración requerida', min: 1, valueAsNumber: true })} 
                  className="input-field" 
                />
                {errors.duracion && <p className="text-red-600 text-sm mt-1">{errors.duracion.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Archivo de audio</label>
                <div className="space-y-2">
                  <input
                    type="file"
                    accept="audio/*"
                    onChange={handleAudioUpload}
                    disabled={isUploadingAudio}
                    className="block w-full text-sm text-gray-700"
                  />
                  {isUploadingAudio && <p className="text-sm text-gray-500">Subiendo audio...</p>}
                  {uploadError && <p className="text-sm text-red-600">{uploadError}</p>}
                  {audioUrlValue ? (
                    <div className="text-sm text-green-600">
                      Audio listo: <span className="break-all">{audioUrlValue}</span>
                    </div>
                  ) : (
                    <p className="text-xs text-gray-500">
                      Selecciona un archivo de audio (mp3, wav, ogg, etc.). Si omites el archivo, se usará un audio de ejemplo.
                    </p>
                  )}
                  {audioUrlValue && (
                    <button type="button" onClick={handleRemoveAudio} className="btn-secondary">
                      Quitar audio
                    </button>
                  )}
                </div>
              </div>
              
              <div className="flex space-x-2">
                <button type="submit" className="btn-primary flex-1">
                  {editingSong ? 'Actualizar' : 'Crear'}
                </button>
                <button type="button" onClick={handleCloseModal} className="btn-secondary">
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Tabla de canciones */}
      <div className="card overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Título</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Artista</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Género</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Año</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Duración</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Audio</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {songs.map((song) => (
              <tr key={song.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{song.id}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{song.titulo}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{song.artista}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{song.genero}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{song.año}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formatDuration(song.duracion)}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {song.audioUrl ? (
                    <a
                      href={resolveAudioUrl(song.audioUrl)}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-primary-600 hover:underline"
                    >
                      Escuchar
                    </a>
                  ) : (
                    <span className="text-gray-400">Sin audio</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                  <button onClick={() => handleEdit(song)} className="text-primary-600 hover:text-primary-900">
                    Editar
                  </button>
                  <button onClick={() => handleDelete(song.id)} className="text-red-600 hover:text-red-900">
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        
        {songs.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500">No hay canciones en el catálogo</p>
          </div>
        )}
      </div>
    </div>
  );
};

