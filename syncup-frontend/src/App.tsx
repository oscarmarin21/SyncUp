import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { AudioPlayerProvider, useAudioPlayer } from '@/context/AudioPlayerContext';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { AudioPlayer } from '@/components/common/AudioPlayer';
import { Login } from '@/pages/Login';
import { Register } from '@/pages/Register';
import { UserHome } from '@/pages/UserHome';
import { AdminHome } from '@/pages/AdminHome';
import { Dashboard } from '@/components/user/Dashboard';
import { Search } from '@/components/user/Search';
import { Favorites } from '@/components/user/Favorites';
import { Recommendations } from '@/components/user/Recommendations';
import { Radio } from '@/components/user/Radio';
import { Social } from '@/components/user/Social';
import { Profile } from '@/components/user/Profile';
import { AdminDashboard } from '@/components/admin/AdminDashboard';
import { SongsManagement } from '@/components/admin/SongsManagement';
import { UsersManagement } from '@/components/admin/UsersManagement';
import { BulkUpload } from '@/components/admin/BulkUpload';
import { Metrics } from '@/components/admin/Metrics';

/**
 * Componente de redirección según rol del usuario autenticado.
 */
const HomeRedirect = () => {
  const { user, isLoading } = useAuth();
  
  if (isLoading) {
    return <div className="flex items-center justify-center min-h-screen">Cargando...</div>;
  }
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  return <Navigate to={user.rol === 'ADMIN' ? '/admin' : '/user'} replace />;
};

/**
 * Componente interno que incluye el reproductor de audio.
 */
const AppWithAudio = () => {
  const { currentSong, nextSong, previousSong, playlist } = useAudioPlayer();
  const { isAuthenticated } = useAuth();

  return (
    <>
      <BrowserRouter>
        <Routes>
          {/* Rutas públicas */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          {/* Redirección por defecto */}
          <Route path="/" element={<HomeRedirect />} />
          
          {/* Rutas protegidas para usuarios */}
          <Route element={<ProtectedRoute requiredRole="USER" />}>
            <Route path="/user" element={<UserHome />}>
              <Route index element={<Dashboard />} />
              <Route path="search" element={<Search />} />
              <Route path="favorites" element={<Favorites />} />
              <Route path="recommendations" element={<Recommendations />} />
              <Route path="radio" element={<Radio />} />
              <Route path="social" element={<Social />} />
              <Route path="profile" element={<Profile />} />
            </Route>
          </Route>
          
          {/* Rutas protegidas para administradores */}
          <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
            <Route path="/admin" element={<AdminHome />}>
              <Route index element={<AdminDashboard />} />
              <Route path="songs" element={<SongsManagement />} />
              <Route path="bulk-upload" element={<BulkUpload />} />
              <Route path="users" element={<UsersManagement />} />
              <Route path="metrics" element={<Metrics />} />
            </Route>
          </Route>
          
          {/* Ruta 404 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      {/* Reproductor de audio fijo en la parte inferior */}
      {isAuthenticated && (
        <AudioPlayer
          song={currentSong}
          onNext={playlist.length > 0 ? nextSong : undefined}
          onPrevious={playlist.length > 0 ? previousSong : undefined}
        />
      )}
    </>
  );
};

/**
 * Componente principal de la aplicación.
 * Configura todas las rutas y protege las que requieren autenticación.
 */
function App() {
  return (
    <AuthProvider>
      <AudioPlayerProvider>
        <AppWithAudio />
      </AudioPlayerProvider>
    </AuthProvider>
  );
}

export default App;
