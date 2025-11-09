import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuth } from '@/context/AuthContext';
import { RegisterRequest } from '@/types/user.types';
import { ErrorMessage } from '@/components/common/ErrorMessage';

/**
 * Página de registro de nuevos usuarios.
 * Requerido según RF-001.
 */
export const Register = () => {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<RegisterRequest & { confirmPassword: string }>();

  const password = watch('password');

  const onSubmit = async (data: RegisterRequest & { confirmPassword: string }) => {
    if (data.password !== data.confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      
      const { confirmPassword, ...registerData } = data;
      await registerUser(registerData);
      
      // Redirigir a página de usuario (nuevos usuarios siempre son USER)
      navigate('/user');
    } catch (err: any) {
      setError(err.message || 'Error al registrar usuario');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Crear Cuenta
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            O{' '}
            <Link to="/login" className="font-medium text-primary-600 hover:text-primary-500">
              inicia sesión
            </Link>
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          {error && <ErrorMessage message={error} onClose={() => setError(null)} />}
          
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Usuario
              </label>
              <input
                {...register('username', { 
                  required: 'El usuario es requerido',
                  minLength: { value: 3, message: 'El usuario debe tener al menos 3 caracteres' }
                })}
                type="text"
                className="input-field mt-1"
                placeholder="Ingresa un usuario"
              />
              {errors.username && (
                <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
              )}
            </div>
            
            <div>
              <label htmlFor="nombre" className="block text-sm font-medium text-gray-700">
                Nombre
              </label>
              <input
                {...register('nombre', { required: 'El nombre es requerido' })}
                type="text"
                className="input-field mt-1"
                placeholder="Ingresa tu nombre"
              />
              {errors.nombre && (
                <p className="mt-1 text-sm text-red-600">{errors.nombre.message}</p>
              )}
            </div>
            
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Contraseña
              </label>
              <input
                {...register('password', { 
                  required: 'La contraseña es requerida',
                  minLength: { value: 6, message: 'La contraseña debe tener al menos 6 caracteres' }
                })}
                type="password"
                className="input-field mt-1"
                placeholder="Ingresa tu contraseña"
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>
            
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                Confirmar Contraseña
              </label>
              <input
                {...register('confirmPassword', { 
                  required: 'Confirma tu contraseña',
                  validate: (value) => value === password || 'Las contraseñas no coinciden'
                })}
                type="password"
                className="input-field mt-1"
                placeholder="Confirma tu contraseña"
              />
              {errors.confirmPassword && (
                <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
              )}
            </div>
          </div>
          
          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Registrando...' : 'Registrarse'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

