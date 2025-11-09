import { Link } from 'react-router-dom';

/**
 * Dashboard principal del administrador.
 * Muestra enlaces a todas las funcionalidades de administración.
 */
export const AdminDashboard = () => {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Panel de Administración</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Link to="/admin/songs" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">🎵 Gestión de Canciones</h2>
          <p className="text-gray-600">Gestiona el catálogo de canciones (CRUD)</p>
        </Link>
        
        <Link to="/admin/bulk-upload" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">📤 Carga Masiva</h2>
          <p className="text-gray-600">Carga múltiples canciones desde un archivo</p>
        </Link>
        
        <Link to="/admin/users" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">👥 Gestión de Usuarios</h2>
          <p className="text-gray-600">Administra los usuarios del sistema</p>
        </Link>
        
        <Link to="/admin/metrics" className="card hover:shadow-lg transition-shadow">
          <h2 className="text-xl font-semibold mb-2">📊 Métricas</h2>
          <p className="text-gray-600">Visualiza estadísticas del sistema</p>
        </Link>
      </div>
    </div>
  );
};

