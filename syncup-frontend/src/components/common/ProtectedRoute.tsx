import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Loading } from './Loading';

interface ProtectedRouteProps {
  requiredRole?: 'USER' | 'ADMIN';
}

/**
 * Componente para proteger rutas que requieren autenticación.
 * Redirige a login si el usuario no está autenticado.
 * Puede requerir un rol específico (USER o ADMIN).
 */
export const ProtectedRoute = ({ requiredRole }: ProtectedRouteProps) => {
  const { isAuthenticated, isAdmin, isLoading } = useAuth();

  if (isLoading) {
    return <Loading />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole === 'ADMIN' && !isAdmin) {
    return <Navigate to="/user" replace />;
  }

  return <Outlet />;
};

