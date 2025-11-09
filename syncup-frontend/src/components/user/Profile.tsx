import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { userService } from '@/services/userService';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { Loading } from '@/components/common/Loading';
import { useAuth } from '@/context/AuthContext';

type ProfileFormValues = {
  nombre: string;
  password: string;
  confirmPassword: string;
};

/**
 * Página de gestión de perfil del usuario.
 * Permite actualizar nombre y contraseña.
 */
export const Profile = () => {
  const { user, updateUser } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [username, setUsername] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    defaultValues: {
      nombre: '',
      password: '',
      confirmPassword: '',
    },
  });

  useEffect(() => {
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadProfile = async () => {
    try {
      setIsLoading(true);
      const profile = await userService.getProfile();
      setUsername(profile.username);
      reset({
        nombre: profile.nombre,
        password: '',
        confirmPassword: '',
      });
    } catch (err: any) {
      setError(err.message || 'Error al cargar el perfil');
    } finally {
      setIsLoading(false);
    }
  };

  const onSubmit = async (data: ProfileFormValues) => {
    setError(null);
    setSuccessMessage(null);

    if (data.password && data.password !== data.confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }

    try {
      const payload: { nombre: string; password?: string } = {
        nombre: data.nombre.trim(),
      };

      if (data.password) {
        payload.password = data.password;
      }

      const updated = await userService.updateProfile(payload);

      updateUser({ nombre: updated.nombre });
      setSuccessMessage('Perfil actualizado correctamente');
      reset({
        nombre: updated.nombre,
        password: '',
        confirmPassword: '',
      });
    } catch (err: any) {
      setError(err.message || 'Error al actualizar el perfil');
    }
  };

  if (isLoading) {
    return <Loading />;
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      <h1 className="text-3xl font-bold mb-6">Mi Perfil</h1>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      {successMessage && (
        <div className="mb-4 p-4 rounded-md bg-green-50 text-green-700 border border-green-200">
          {successMessage}
        </div>
      )}

      <div className="card">
        <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Usuario</label>
            <input
              value={username || user?.username || ''}
              disabled
              className="input-field bg-gray-100 cursor-not-allowed"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
            <input
              {...register('nombre', { required: 'El nombre es obligatorio' })}
              className="input-field"
            />
            {errors.nombre && (
              <p className="text-red-600 text-sm mt-1">{errors.nombre.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nueva contraseña
            </label>
            <input
              type="password"
              {...register('password')}
              className="input-field"
              placeholder="Dejar en blanco para mantener la actual"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Confirmar nueva contraseña
            </label>
            <input
              type="password"
              {...register('confirmPassword')}
              className="input-field"
              placeholder="Repite la nueva contraseña"
            />
          </div>

  <div className="text-sm text-gray-500">
            Las contraseñas se guardan cifradas. Si no deseas cambiarla, deja los campos en blanco.
          </div>

          <div className="flex justify-end">
            <button type="submit" className="btn-primary">
              Guardar Cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};


