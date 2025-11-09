import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useAuth } from '@/context/AuthContext';
import { LoginRequest } from '@/types/user.types';
import { ErrorMessage } from '@/components/common/ErrorMessage';

/**
 * Página de inicio de sesión.
 * Requerido según RF-001.
 */
export const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginRequest>();

  const onSubmit = async (data: LoginRequest) => {
    try {
      setIsLoading(true);
      setError(null);
      await login(data);
      
      // Redirigir según el rol
      const user = JSON.parse(localStorage.getItem('syncup_user') || '{}');
      if (user.rol === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/user');
      }
    } catch (err: any) {
      setError(err.message || 'Error al iniciar sesión');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Iniciar Sesión
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            O{' '}
            <Link to="/register" className="font-medium text-primary-600 hover:text-primary-500">
              crea una cuenta nueva
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
                {...register('username', { required: 'El usuario es requerido' })}
                type="text"
                className="input-field mt-1"
                placeholder="Ingresa tu usuario"
              />
              {errors.username && (
                <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
              )}
            </div>
            
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Contraseña
              </label>
              <input
                {...register('password', { required: 'La contraseña es requerida' })}
                type="password"
                className="input-field mt-1"
                placeholder="Ingresa tu contraseña"
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>
          </div>
          
          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </button>
          </div>
          
          <div className="text-center text-sm text-gray-600">
          </div>
        </form>
      </div>
    </div>
  );
};

