import { Link } from 'react-router-dom';

/**
 * Dashboard principal del usuario.
 * Muestra enlaces a todas las funcionalidades disponibles.
 */
export const Dashboard = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Bienvenido a SyncUp</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Link to="/user/search" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">🔍 Buscar Canciones</h2>
          <p className="text-gray-600">Busca canciones por título o usa búsqueda avanzada</p>
        </Link>
        
        <Link to="/user/favorites" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">❤️ Mis Favoritos</h2>
          <p className="text-gray-600">Gestiona tu lista de canciones favoritas</p>
        </Link>
        
        <Link to="/user/recommendations" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">🎵 Descubrimiento Semanal</h2>
          <p className="text-gray-600">Descubre nuevas canciones basadas en tus gustos</p>
        </Link>
        
        <Link to="/user/radio" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">📻 Radio</h2>
          <p className="text-gray-600">Inicia una radio desde cualquier canción</p>
        </Link>
        
        <Link to="/user/social" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">👥 Social</h2>
          <p className="text-gray-600">Conecta con otros usuarios y descubre nuevos gustos</p>
        </Link>
      </div>
    </div>
  );
};

