# SyncUp Frontend

Frontend de la plataforma de recomendación musical SyncUp, desarrollado con React 18, TypeScript y Tailwind CSS.

## 🚀 Tecnologías

- **React 18** - Biblioteca de UI
- **TypeScript** - Tipado estático
- **Vite** - Build tool y servidor de desarrollo
- **React Router DOM** - Enrutamiento
- **Axios** - Cliente HTTP
- **Tailwind CSS** - Framework de estilos
- **Recharts** - Gráficos para métricas
- **React Hook Form** - Manejo de formularios

## 📋 Requisitos Previos

- Node.js 18+ y npm

## 🔧 Instalación

1. Instalar dependencias:

```bash
npm install
```

2. Configurar variables de entorno (opcional):

Crear un archivo `.env` con:

```
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🏃 Desarrollo

Iniciar el servidor de desarrollo:

```bash
npm run dev
```

La aplicación estará disponible en `http://localhost:5173`

## 🏗️ Build para Producción

```bash
npm run build
```

Los archivos de producción estarán en la carpeta `dist/`

## 📁 Estructura del Proyecto

```
src/
├── components/          # Componentes React
│   ├── common/         # Componentes compartidos
│   ├── user/           # Componentes de usuario
│   ├── admin/          # Componentes de administrador
│   └── charts/         # Componentes de gráficos
├── pages/              # Páginas principales
├── services/           # Servicios de API
├── hooks/              # Hooks personalizados
├── context/            # Context API
├── types/               # Tipos TypeScript
└── utils/              # Utilidades
```

## 🎯 Funcionalidades Implementadas

### Usuario (RF-001 a RF-009)
- ✅ Registro e inicio de sesión
- ✅ Búsqueda con autocompletado (RF-003)
- ✅ Búsqueda avanzada (RF-004)
- ✅ Gestión de favoritos (RF-002)
- ✅ Descubrimiento semanal (RF-005)
- ✅ Radio (RF-006)
- ✅ Funcionalidades sociales (RF-007, RF-008)
- ✅ Exportación CSV de favoritos (RF-009)

### Administrador (RF-010 a RF-014)
- ✅ Gestión de canciones CRUD (RF-010)
- ✅ Gestión de usuarios (RF-011)
- ✅ Carga masiva de canciones (RF-012)
- ✅ Métricas y gráficos (RF-013, RF-014)

## 🔐 Autenticación

La aplicación utiliza JWT para autenticación. El token se almacena en `localStorage` y se incluye automáticamente en todas las peticiones.

## 📝 Notas

- El backend debe estar corriendo en `http://localhost:8080`
- Las rutas protegidas redirigen automáticamente a `/login` si no hay autenticación
- Los administradores tienen acceso a rutas adicionales (`/admin/*`)
