import { useState, useEffect } from 'react';

/**
 * Hook personalizado para debounce de valores.
 * Útil para optimizar búsquedas y evitar demasiadas requests.
 * 
 * @param value valor a debounce
 * @param delay delay en milisegundos
 * @returns valor debounced
 */
export function useDebounce<T>(value: T, delay: number = 500): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

