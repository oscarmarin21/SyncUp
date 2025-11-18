import { useState, useEffect, KeyboardEvent } from 'react';
import { useLocation } from 'react-router-dom';
import { socialService } from '@/services/socialService';
import { User } from '@/types/user.types';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';

/**
 * Componente para funcionalidades sociales.
 * Requerido según RF-007 y RF-008.
 */
export const Social = () => {
  const location = useLocation();
  const [suggestions, setSuggestions] = useState<User[]>([]);
  const [following, setFollowing] = useState<User[]>([]);
  const [followers, setFollowers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionUser, setActionUser] = useState<string | null>(null);

  // 🔍 estados de búsqueda
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState<User[] | null>(null);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [sug, foll, follwers] = await Promise.all([
        socialService.getSuggestions(10),
        socialService.getFollowing(),
        socialService.getFollowers(),
      ]);
      setSuggestions(sug);
      setFollowing(foll);
      setFollowers(follwers);
    } catch (err: any) {
      setError(err.message || 'Error al cargar la información social');
    } finally {
      setIsLoading(false);
      setActionUser(null);
    }
  };

  const handleFollow = async (username: string) => {
    try {
      setActionUser(username);
      await socialService.followUser(username);
      await loadData();
    } catch (err: any) {
      setError(err.message || 'Error al seguir usuario');
    }
  };

  const handleUnfollow = async (username: string) => {
    try {
      setActionUser(username);
      await socialService.unfollowUser(username);
      await loadData();
    } catch (err: any) {
      setError(err.message || 'Error al dejar de seguir al usuario');
    }
  };

  const isFollowingUser = (username: string) =>
    following.some((user) => user.username === username);

  // 🔍 buscar usuarios en el backend
  const performSearch = async () => {
    const term = searchTerm.trim();
    if (!term) {
      setSearchResults(null);
      return;
    }

    try {
      setIsSearching(true);
      const results = await socialService.searchUsers(term);
      setSearchResults(results);
    } catch (err: any) {
      console.error('Error buscando usuarios:', err);
      setError(err.message || 'Error al buscar usuarios');
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearchClick = () => {
    performSearch();
  };

  const handleSearchKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      performSearch();
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Conecta con la comunidad</h1>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      {/* 🔍 barra de búsqueda */}
      <div className="mb-8 max-w-xl">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Buscar usuarios
        </label>
        <div className="flex gap-2">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={handleSearchKeyDown}
            placeholder="Busca por nombre o @username..."
            className="input-field flex-1"
          />
          <button
            type="button"
            onClick={handleSearchClick}
            className="inline-flex items-center justify-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-100 transition-colors"
            title="Buscar usuarios"
          >
            <svg
              className="w-5 h-5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M12.9 14.32a8 8 0 111.414-1.414l3.387 3.387a1 1 0 01-1.414 1.414l-3.387-3.387zM14 8a6 6 0 11-12 0 6 6 0 0112 0z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </div>
        {isSearching && (
          <p className="mt-2 text-xs text-gray-500">Buscando usuarios...</p>
        )}
        {searchResults && !isSearching && (
          <p className="mt-2 text-xs text-gray-500">
            {searchResults.length} resultado(s) encontrados
          </p>
        )}
      </div>

      {isLoading ? (
        <Loading />
      ) : (
        <div className="space-y-10">
          {/* 🔍 Resultados de búsqueda */}
          {searchResults && (
            <section>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-2xl font-semibold">Resultados de búsqueda</h2>
                <span className="text-sm text-gray-500">
                  {searchResults.length} usuarios
                </span>
              </div>

              {searchResults.length === 0 ? (
                <div className="card text-center py-6">
                  <p className="text-gray-500">
                    No se encontraron usuarios que coincidan con &quot;{searchTerm}&quot;.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {searchResults.map((user) => {
                    const alreadyFollowing = isFollowingUser(user.username);
                    return (
                      <div
                        key={user.id}
                        className="card hover:shadow-md transition-shadow"
                      >
                        <h3 className="font-semibold text-lg mb-1">{user.nombre}</h3>
                        <p className="text-gray-600 mb-4">@{user.username}</p>
                        <button
                          onClick={() =>
                            alreadyFollowing
                              ? handleUnfollow(user.username)
                              : handleFollow(user.username)
                          }
                          className={`w-full ${
                            alreadyFollowing ? 'btn-secondary' : 'btn-primary'
                          }`}
                          disabled={actionUser === user.username}
                        >
                          {actionUser === user.username
                            ? 'Procesando...'
                            : alreadyFollowing
                            ? 'Dejar de seguir'
                            : 'Seguir'}
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}
            </section>
          )}

          {/* Sugerencias para seguir */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Sugerencias para seguir</h2>
              <span className="text-sm text-gray-500">
                {suggestions.length} usuarios sugeridos
              </span>
            </div>

            {suggestions.length === 0 ? (
              <div className="text-center py-8 card">
                <p className="text-gray-500 text-lg">No hay sugerencias por ahora.</p>
                <p className="text-gray-400 mt-2">
                  Sigue a más personas para recibir nuevas recomendaciones.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {suggestions.map((user) => (
                  <div
                    key={user.id}
                    className="card hover:shadow-md transition-shadow"
                  >
                    <h3 className="font-semibold text-lg mb-1">{user.nombre}</h3>
                    <p className="text-gray-600 mb-4">@{user.username}</p>
                    <button
                      onClick={() => handleFollow(user.username)}
                      className="btn-primary w-full"
                      disabled={actionUser === user.username}
                    >
                      {actionUser === user.username ? 'Procesando...' : 'Seguir'}
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Personas que sigues */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Personas que sigues</h2>
              <span className="text-sm text-gray-500">
                {following.length} usuarios
              </span>
            </div>

            {following.length === 0 ? (
              <div className="card text-center py-6">
                <p className="text-gray-500">Aún no sigues a nadie.</p>
                <p className="text-gray-400 text-sm mt-1">
                  Explora las sugerencias para comenzar.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {following.map((user) => (
                  <div
                    key={user.id}
                    className="card hover:shadow-md transition-shadow"
                  >
                    <h3 className="font-semibold text-lg mb-1">{user.nombre}</h3>
                    <p className="text-gray-600 mb-4">@{user.username}</p>
                    <button
                      onClick={() => handleUnfollow(user.username)}
                      className="btn-secondary w-full"
                      disabled={actionUser === user.username}
                    >
                      {actionUser === user.username ? 'Procesando...' : 'Dejar de seguir'}
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Tus seguidores */}
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Tus seguidores</h2>
              <span className="text-sm text-gray-500">
                {followers.length} usuarios
              </span>
            </div>

            {followers.length === 0 ? (
              <div className="card text-center py-6">
                <p className="text-gray-500">Aún no tienes seguidores.</p>
                <p className="text-gray-400 text-sm mt-1">
                  Interactúa con otros usuarios para que te descubran.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {followers.map((user) => {
                  const alreadyFollowing = isFollowingUser(user.username);
                  return (
                    <div
                      key={user.id}
                      className="card hover:shadow-md transition-shadow"
                    >
                      <h3 className="font-semibold text-lg mb-1">{user.nombre}</h3>
                      <p className="text-gray-600 mb-4">@{user.username}</p>
                      <div className="space-y-2">
                        <span className="inline-block px-2 py-1 text-xs font-semibold rounded bg-primary-100 text-primary-700">
                          Te sigue
                        </span>
                        <button
                          onClick={() =>
                            alreadyFollowing
                              ? handleUnfollow(user.username)
                              : handleFollow(user.username)
                          }
                          className={`w-full ${
                            alreadyFollowing ? 'btn-secondary' : 'btn-primary'
                          }`}
                          disabled={actionUser === user.username}
                        >
                          {actionUser === user.username
                            ? 'Procesando...'
                            : alreadyFollowing
                            ? 'Dejar de seguir'
                            : 'Seguir también'}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  );
};
