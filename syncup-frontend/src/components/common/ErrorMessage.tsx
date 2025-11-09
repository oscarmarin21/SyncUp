interface ErrorMessageProps {
  message: string;
  onClose?: () => void;
}

/**
 * Componente para mostrar mensajes de error.
 */
export const ErrorMessage = ({ message, onClose }: ErrorMessageProps) => {
  return (
    <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 flex items-center justify-between">
      <span>{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          className="text-red-700 hover:text-red-900 ml-4"
          aria-label="Cerrar"
        >
          ×
        </button>
      )}
    </div>
  );
};

