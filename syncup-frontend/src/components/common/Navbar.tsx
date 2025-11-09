import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

/**
 * Barra de navegación principal.
 * Muestra diferentes opciones según el rol del usuario.
 */
export const Navbar = () => {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) {
    return null;
  }

  return (
    <nav className="bg-white border-b border-gray-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center space-x-8">
            <Link to={isAdmin ? '/admin' : '/user'} className="text-xl font-bold text-primary-600">
              SyncUp
            </Link>
            
            {isAdmin ? (
              <div className="flex space-x-4">
                <Link to="/admin/songs" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Canciones
                </Link>
                <Link to="/admin/users" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Usuarios
                </Link>
                <Link to="/admin/metrics" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Métricas
                </Link>
              </div>
            ) : (
              <div className="flex space-x-4">
                <Link to="/user/search" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Buscar
                </Link>
                <Link to="/user/favorites" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Favoritos
                </Link>
                <Link to="/user/recommendations" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Recomendaciones
                </Link>
                <Link to="/user/social" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Social
                </Link>
                <Link to="/user/profile" className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium">
                  Perfil
                </Link>
              </div>
            )}
          </div>
          
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-700">{user.nombre}</span>
            <button
              onClick={handleLogout}
              className="text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

