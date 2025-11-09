import { Outlet, useLocation } from 'react-router-dom';
import { Navbar } from '@/components/common/Navbar';

/**
 * Layout principal para usuarios.
 * Contiene el Navbar y muestra las páginas hijas.
 */
export const UserHome = () => {
  const location = useLocation();
  
  return (
    <div className="min-h-screen bg-gray-50 pb-24">
      <Navbar />
      <main>
        {/* Usar key basado en location.pathname para forzar re-render cuando cambia la ruta */}
        <Outlet key={location.pathname} />
      </main>
    </div>
  );
};

