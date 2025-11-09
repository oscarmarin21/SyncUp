import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface ArtistBarChartProps {
  data: Record<string, number>;
}

/**
 * Componente Bar Chart para mostrar artistas más populares.
 * Requerido según RF-014.
 */
export const ArtistBarChart = ({ data }: ArtistBarChartProps) => {
  const chartData = Object.entries(data)
    .map(([name, value]) => ({
      name: name.length > 15 ? name.substring(0, 15) + '...' : name,
      fullName: name,
      canciones: value,
    }))
    .sort((a, b) => b.canciones - a.canciones);

  if (chartData.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-gray-500">
        No hay datos disponibles
      </div>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={400}>
      <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis 
          dataKey="name" 
          angle={-45} 
          textAnchor="end" 
          height={100}
        />
        <YAxis />
        <Tooltip 
          formatter={(value: number) => [`${value} canciones`, 'Cantidad']}
          labelFormatter={(label, payload) => payload?.[0]?.payload?.fullName || label}
        />
        <Legend />
        <Bar dataKey="canciones" fill="#0ea5e9" name="Canciones" />
      </BarChart>
    </ResponsiveContainer>
  );
};

