import { useState } from 'react';
import { adminService } from '@/services/adminService';
import { ErrorMessage } from '@/components/common/ErrorMessage';

/**
 * Componente para carga masiva de canciones.
 * Requerido según RF-012.
 */
export const BulkUpload = () => {
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<{
    cancionesCargadas: number;
    errores: number;
    erroresDetalle: string[];
  } | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      setResult(null);
      setError(null);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Selecciona un archivo');
      return;
    }

    try {
      setIsUploading(true);
      setError(null);
      const uploadResult = await adminService.bulkUpload(file);
      setResult(uploadResult);
    } catch (err: any) {
      setError(err.message || 'Error al cargar archivo');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Carga Masiva de Canciones</h1>

      {error && <ErrorMessage message={error} onClose={() => setError(null)} />}

      <div className="card max-w-2xl">
        <h2 className="text-xl font-semibold mb-4">Formato del Archivo</h2>
        <p className="text-gray-600 mb-4">
          El archivo debe ser un archivo de texto plano (.txt) con el siguiente formato:
        </p>
        <div className="bg-gray-100 p-4 rounded-lg mb-4 font-mono text-sm">
          <p>Título|Artista|Género|Año|Duración</p>
          <p className="text-gray-500 mt-2">Ejemplo:</p>
          <p>Bohemian Rhapsody|Queen|Rock|1975|355</p>
          <p>Another One Bites the Dust|Queen|Rock|1980|216</p>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Seleccionar Archivo
            </label>
            <input
              type="file"
              accept=".txt"
              onChange={handleFileChange}
              className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-primary-50 file:text-primary-700 hover:file:bg-primary-100"
            />
          </div>

          <button
            onClick={handleUpload}
            disabled={!file || isUploading}
            className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isUploading ? 'Cargando...' : 'Cargar Canciones'}
          </button>
        </div>

        {result && (
          <div className="mt-6 p-4 bg-gray-50 rounded-lg">
            <h3 className="font-semibold mb-2">Resultados de la Carga</h3>
            <p className="text-green-600">✓ Canciones cargadas: {result.cancionesCargadas}</p>
            <p className="text-red-600">✗ Errores: {result.errores}</p>
            
            {result.erroresDetalle.length > 0 && (
              <div className="mt-4">
                <p className="font-semibold mb-2">Detalles de errores:</p>
                <ul className="list-disc list-inside text-sm text-red-600 space-y-1">
                  {result.erroresDetalle.map((error, index) => (
                    <li key={index}>{error}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

