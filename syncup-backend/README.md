# SyncUp Backend - Motor de Recomendaciones Musicales

Backend Spring Boot para la plataforma SyncUp, implementando estructuras de datos avanzadas (Grafos, Trie, HashMap, LinkedList) según los requerimientos del proyecto.

## Tecnologías

- **Spring Boot 3.2.0**
- **SQLite** (base de datos liviana)
- **Spring Security + JWT** (autenticación)
- **JUnit 5 + Mockito** (pruebas unitarias)
- **Lombok** (reducción de boilerplate)

## Estructura del Proyecto

```
src/main/java/com/syncup/
├── config/              # Configuración (Security, Async, Data Initializer)
├── controller/          # REST Controllers (Auth, Canciones, Usuario, Admin, etc.)
├── dto/                 # Data Transfer Objects
├── exception/           # Manejo de excepciones globales
├── graph/              # Grafos (Similitud, Social) y algoritmos (Dijkstra, BFS)
├── model/              # Entidades JPA (Usuario, Cancion)
├── repository/         # Repositorios JPA
├── security/           # Spring Security + JWT
├── service/            # Servicios de negocio
├── trie/               # Trie para autocompletado
└── util/               # Utilidades (CSV Exporter)
```

## Requerimientos Implementados

### Usuario (RF-001 a RF-009)
- ✅ RF-001: Registro e inicio de sesión con JWT
- ✅ RF-002: Gestión de perfil y favoritos (LinkedList)
- ✅ RF-003: Búsqueda por autocompletado (Trie)
- ✅ RF-004: Búsqueda avanzada con concurrencia
- ✅ RF-005: Descubrimiento Semanal (Dijkstra en grafo de similitud)
- ✅ RF-006: Radio (grafo de similitud)
- ✅ RF-007: Seguir/dejar de seguir usuarios
- ✅ RF-008: Sugerencias de usuarios (BFS)
- ✅ RF-009: Exportación CSV de favoritos

### Administrador (RF-010 a RF-014)
- ✅ RF-010: CRUD de canciones
- ✅ RF-011: Gestión de usuarios
- ✅ RF-012: Carga masiva de canciones desde archivo
- ✅ RF-013: Panel de métricas
- ✅ RF-014: Métricas para gráficos (géneros, artistas)

### Estructuras de Datos
- ✅ RF-015: LinkedList para favoritos
- ✅ RF-016: HashMap para usuarios (acceso O(1))
- ✅ RF-017: hashCode/equals en Usuario
- ✅ RF-018: Entidad Cancion
- ✅ RF-019: Canciones como nodos en grafo
- ✅ RF-020: hashCode/equals en Cancion
- ✅ RF-021: Grafo Ponderado No Dirigido (similitud)
- ✅ RF-022: Algoritmo Dijkstra
- ✅ RF-023: Grafo No Dirigido (social)
- ✅ RF-024: Algoritmo BFS
- ✅ RF-025: Trie para autocompletado
- ✅ RF-026: Búsqueda por prefijo en Trie

### Técnicos
- ✅ RF-027: (Diagrama de clases - documento externo)
- ✅ RF-028: Proyecto funcional en Java
- ✅ RF-029: Exportación CSV
- ✅ RF-030: Búsqueda concurrente (@Async)
- ✅ RF-031: 7+ tests unitarios
- ✅ RF-032: JavaDoc en todas las clases públicas

## Configuración

### application.properties

```properties
server.port=8080
spring.datasource.url=jdbc:sqlite:syncup.db
jwt.secret=SyncUpSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits
jwt.expiration=86400000
cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

## Compilación y Ejecución

### Requisitos
- Java 17+
- Maven 3.6+

### Compilar
```bash
cd syncup-backend
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### Ejecutar Tests
```bash
mvn test
```

### Generar JavaDoc
```bash
mvn javadoc:javadoc
```

## API Endpoints

### Autenticación
- `POST /api/auth/register` - Registro
- `POST /api/auth/login` - Login

### Canciones
- `GET /api/songs/autocomplete?prefix={prefix}` - Autocompletado
- `POST /api/songs/search/advanced` - Búsqueda avanzada
- `GET /api/songs/{id}` - Obtener por ID

### Usuario
- `GET /api/users/me` - Perfil actual
- `PUT /api/users/me` - Actualizar perfil
- `GET /api/users/me/favorites` - Favoritos
- `POST /api/users/me/favorites/{songId}` - Agregar favorito
- `DELETE /api/users/me/favorites/{songId}` - Eliminar favorito
- `GET /api/users/me/favorites/export` - Exportar CSV

### Recomendaciones
- `GET /api/recommendations/discovery-weekly` - Descubrimiento Semanal
- `POST /api/recommendations/radio?songId={id}` - Iniciar Radio

### Social
- `POST /api/users/{username}/follow` - Seguir usuario
- `DELETE /api/users/{username}/follow` - Dejar de seguir
- `GET /api/users/suggestions` - Sugerencias

### Admin (requiere rol ADMIN)
- `GET /api/admin/songs` - Listar canciones
- `POST /api/admin/songs` - Crear canción
- `PUT /api/admin/songs/{id}` - Actualizar canción
- `DELETE /api/admin/songs/{id}` - Eliminar canción
- `POST /api/admin/songs/bulk-upload` - Carga masiva
- `GET /api/admin/users` - Listar usuarios
- `DELETE /api/admin/users/{username}` - Eliminar usuario
- `GET /api/admin/metrics/genres` - Métricas géneros
- `GET /api/admin/metrics/artists` - Métricas artistas

## Datos de Prueba

Al iniciar la aplicación, se cargan automáticamente datos de prueba:

### Usuarios
- **Admin:** `admin` / `admin123` (Rol: ADMIN)
- **Usuario 1:** `juan` / `password123` (Rol: USER)
- **Usuario 2:** `maria` / `password123` (Rol: USER)
- **Usuario 3:** `carlos` / `password123` (Rol: USER)
- **Usuario 4:** `ana` / `password123` (Rol: USER)

### Canciones
Se crean **20 canciones** de prueba de diferentes géneros (Rock, Pop, Grunge, Funk, etc.) y artistas (Queen, Michael Jackson, Eagles, Led Zeppelin, etc.).

### Datos Relacionados
- **Favoritos:** Los usuarios `juan`, `maria` y `carlos` tienen canciones favoritas pre-configuradas
- **Relaciones Sociales:** Se configuran automáticamente conexiones entre usuarios para probar las sugerencias

📄 Ver el archivo `DATOS_DE_PRUEBA.md` en la raíz del proyecto para más detalles.

## Notas

- Las estructuras de datos (Grafos, Trie, HashMap) se cargan en memoria al arranque
- El grafo de similitud se construye calculando similitudes entre todas las canciones
- SQLite crea el archivo `syncup.db` en la raíz del proyecto
- Todos los endpoints (excepto `/api/auth/**`) requieren autenticación JWT

## Próximos Pasos

Para el frontend React:
1. Crear proyecto React con TypeScript
2. Configurar Axios para llamadas API
3. Implementar autenticación con JWT
4. Crear componentes de usuario y administrador
5. Integrar Recharts para gráficos de métricas

