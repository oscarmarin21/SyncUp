import { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import { Loading } from '@/components/common/Loading';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { GenrePieChart } from '@/components/charts/GenrePieChart';
import { ArtistBarChart } from '@/components/charts/ArtistBarChart';

/**
 * Componente para panel de métricas del sistema.
 * Requerido según RF-013 y RF-014.
 */
export const Metrics = () => {
  const [genreMetrics, setGenreMetrics] = useState<Record<string, number>>({});
  const [artistMetrics, setArtistMetrics] = useState<Record<string, number>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadMetrics();
  }, []);

  const loadMetrics = async () => {
    try {
      setIsLoading(true);
      const [genres, artists] = await Promise.all([
        adminService.getGenreMetrics(),
        adminService.getArtistMetrics(10),
      ]);
      setGenreMetrics(genres);
      setArtistMetrics(artists);
    } catch (err: any) {
      setError(err.message || 'Error al cargar métricas');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <Loading />;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Métricas del Sistema</h1>
        <button onClick={loadMetrics} className="btn-secondary">
          Actualizar
        </button>
      </div>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Distribución de Géneros</h2>
          <GenrePieChart data={genreMetrics} />
        </div>

        <div className="card">
          <h2 className="text-xl font-semibold mb-4">Top 10 Artistas Más Populares</h2>
          <ArtistBarChart data={artistMetrics} />
        </div>
      </div>
    </div>
  );
};

