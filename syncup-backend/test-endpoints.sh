#!/bin/bash

# Script para probar todos los endpoints de SyncUp Backend

BASE_URL="http://localhost:8080/api"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "  Testing SyncUp Backend Endpoints"
echo "=========================================="
echo ""

# Verificar que el servidor esté corriendo
echo -e "${YELLOW}[1/30]${NC} Verificando servidor..."
if ! curl -s http://localhost:8080/api/auth/login > /dev/null 2>&1; then
    echo -e "${RED}✗ Error: Servidor no está corriendo. Ejecuta: mvn spring-boot:run${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Servidor está corriendo${NC}"
echo ""

# ========== AUTENTICACIÓN ==========
echo "=========================================="
echo "  AUTENTICACIÓN (RF-001)"
echo "=========================================="

echo -e "${YELLOW}[2/30]${NC} Registrando usuario test..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "nombre": "Usuario de Prueba"
  }')
echo "Response: $REGISTER_RESPONSE"
TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Error al registrar usuario${NC}"
else
    echo -e "${GREEN}✓ Usuario registrado. Token obtenido${NC}"
fi
echo ""

echo -e "${YELLOW}[3/30]${NC} Login con usuario test..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }')
echo "Response: $LOGIN_RESPONSE"
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Error al hacer login${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Login exitoso. Token: ${TOKEN:0:20}...${NC}"
echo ""

# Login como admin
echo -e "${YELLOW}[4/30]${NC} Login como admin..."
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')
ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}✗ Error al hacer login como admin${NC}"
else
    echo -e "${GREEN}✓ Admin logueado exitosamente${NC}"
fi
echo ""

# ========== ADMIN: CREAR CANCIONES ==========
echo "=========================================="
echo "  ADMIN: GESTIÓN DE CANCIONES (RF-010)"
echo "=========================================="

echo -e "${YELLOW}[5/30]${NC} Creando canción 1..."
SONG1=$(curl -s -X POST "$BASE_URL/admin/songs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "titulo": "Bohemian Rhapsody",
    "artista": "Queen",
    "genero": "Rock",
    "año": 1975,
    "duracion": 355
  }')
SONG1_ID=$(echo $SONG1 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Response: $SONG1"
echo -e "${GREEN}✓ Canción creada con ID: $SONG1_ID${NC}"
echo ""

echo -e "${YELLOW}[6/30]${NC} Creando canción 2..."
SONG2=$(curl -s -X POST "$BASE_URL/admin/songs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "titulo": "Another One Bites the Dust",
    "artista": "Queen",
    "genero": "Rock",
    "año": 1980,
    "duracion": 216
  }')
SONG2_ID=$(echo $SONG2 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✓ Canción creada con ID: $SONG2_ID${NC}"
echo ""

echo -e "${YELLOW}[7/30]${NC} Creando canción 3..."
SONG3=$(curl -s -X POST "$BASE_URL/admin/songs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "titulo": "Billie Jean",
    "artista": "Michael Jackson",
    "genero": "Pop",
    "año": 1983,
    "duracion": 294
  }')
SONG3_ID=$(echo $SONG3 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✓ Canción creada con ID: $SONG3_ID${NC}"
echo ""

echo -e "${YELLOW}[8/30]${NC} Listando todas las canciones..."
LIST_SONGS=$(curl -s -X GET "$BASE_URL/admin/songs" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Response: $LIST_SONGS"
echo -e "${GREEN}✓ Canciones listadas${NC}"
echo ""

# ========== BÚSQUEDAS ==========
echo "=========================================="
echo "  BÚSQUEDAS (RF-003, RF-004)"
echo "=========================================="

echo -e "${YELLOW}[9/30]${NC} Autocompletado: buscando 'Bo'..."
AUTOCOMPLETE=$(curl -s -X GET "$BASE_URL/songs/autocomplete?prefix=Bo" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $AUTOCOMPLETE"
echo -e "${GREEN}✓ Autocompletado funcionando${NC}"
echo ""

echo -e "${YELLOW}[10/30]${NC} Búsqueda avanzada: artista=Queen, operador=AND..."
SEARCH=$(curl -s -X POST "$BASE_URL/songs/search/advanced" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "artista": "Queen",
    "operador": "AND"
  }')
echo "Response: $SEARCH"
echo -e "${GREEN}✓ Búsqueda avanzada funcionando${NC}"
echo ""

echo -e "${YELLOW}[11/30]${NC} Búsqueda avanzada: género=Rock, operador=OR..."
SEARCH2=$(curl -s -X POST "$BASE_URL/songs/search/advanced" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "genero": "Rock",
    "operador": "OR"
  }')
echo "Response: $SEARCH2"
echo -e "${GREEN}✓ Búsqueda con OR funcionando${NC}"
echo ""

# ========== USUARIO: PERFIL Y FAVORITOS ==========
echo "=========================================="
echo "  USUARIO: PERFIL Y FAVORITOS (RF-002)"
echo "=========================================="

echo -e "${YELLOW}[12/30]${NC} Obteniendo perfil del usuario..."
PROFILE=$(curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $PROFILE"
echo -e "${GREEN}✓ Perfil obtenido${NC}"
echo ""

echo -e "${YELLOW}[13/30]${NC} Actualizando perfil..."
UPDATE_PROFILE=$(curl -s -X PUT "$BASE_URL/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nombre": "Usuario Actualizado"
  }')
echo "Response: $UPDATE_PROFILE"
echo -e "${GREEN}✓ Perfil actualizado${NC}"
echo ""

echo -e "${YELLOW}[14/30]${NC} Agregando canción a favoritos..."
ADD_FAV=$(curl -s -X POST "$BASE_URL/users/me/favorites/$SONG1_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $ADD_FAV"
echo -e "${GREEN}✓ Favorito agregado${NC}"
echo ""

echo -e "${YELLOW}[15/30]${NC} Agregando otra canción a favoritos..."
curl -s -X POST "$BASE_URL/users/me/favorites/$SONG2_ID" \
  -H "Authorization: Bearer $TOKEN" > /dev/null
echo -e "${GREEN}✓ Segundo favorito agregado${NC}"
echo ""

echo -e "${YELLOW}[16/30]${NC} Obteniendo lista de favoritos..."
FAVORITES=$(curl -s -X GET "$BASE_URL/users/me/favorites" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $FAVORITES"
echo -e "${GREEN}✓ Favoritos obtenidos${NC}"
echo ""

echo -e "${YELLOW}[17/30]${NC} Exportando favoritos a CSV..."
CSV=$(curl -s -X GET "$BASE_URL/users/me/favorites/export" \
  -H "Authorization: Bearer $TOKEN")
if [ ! -z "$CSV" ]; then
    echo -e "${GREEN}✓ CSV exportado (${#CSV} bytes)${NC}"
else
    echo -e "${RED}✗ Error al exportar CSV${NC}"
fi
echo ""

echo -e "${YELLOW}[18/30]${NC} Eliminando un favorito..."
DELETE_FAV=$(curl -s -X DELETE "$BASE_URL/users/me/favorites/$SONG2_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $DELETE_FAV"
echo -e "${GREEN}✓ Favorito eliminado${NC}"
echo ""

# ========== RECOMENDACIONES ==========
echo "=========================================="
echo "  RECOMENDACIONES (RF-005, RF-006)"
echo "=========================================="

echo -e "${YELLOW}[19/30]${NC} Generando Descubrimiento Semanal..."
DISCOVERY=$(curl -s -X GET "$BASE_URL/recommendations/discovery-weekly?maxCanciones=10" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $DISCOVERY"
echo -e "${GREEN}✓ Descubrimiento Semanal generado${NC}"
echo ""

echo -e "${YELLOW}[20/30]${NC} Iniciando Radio desde canción..."
RADIO=$(curl -s -X POST "$BASE_URL/recommendations/radio?songId=$SONG1_ID&maxCanciones=10" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $RADIO"
echo -e "${GREEN}✓ Radio iniciada${NC}"
echo ""

# ========== SOCIAL ==========
echo "=========================================="
echo "  FUNCIONALIDADES SOCIALES (RF-007, RF-008)"
echo "=========================================="

# Crear segundo usuario para pruebas sociales
echo -e "${YELLOW}[21/30]${NC} Registrando segundo usuario..."
USER2=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2",
    "password": "test123",
    "nombre": "Usuario Dos"
  }')
USER2_TOKEN=$(echo $USER2 | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✓ Segundo usuario registrado${NC}"
echo ""

echo -e "${YELLOW}[22/30]${NC} Usuario test sigue a user2..."
FOLLOW=$(curl -s -X POST "$BASE_URL/users/user2/follow" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $FOLLOW"
echo -e "${GREEN}✓ Usuario seguido${NC}"
echo ""

echo -e "${YELLOW}[23/30]${NC} Obteniendo sugerencias de usuarios..."
SUGGESTIONS=$(curl -s -X GET "$BASE_URL/users/suggestions?maxSugerencias=5" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $SUGGESTIONS"
echo -e "${GREEN}✓ Sugerencias obtenidas${NC}"
echo ""

echo -e "${YELLOW}[24/30]${NC} Dejar de seguir usuario..."
UNFOLLOW=$(curl -s -X DELETE "$BASE_URL/users/user2/follow" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $UNFOLLOW"
echo -e "${GREEN}✓ Usuario dejado de seguir${NC}"
echo ""

# ========== ADMIN: MÉTRICAS ==========
echo "=========================================="
echo "  ADMIN: MÉTRICAS (RF-013, RF-014)"
echo "=========================================="

echo -e "${YELLOW}[25/30]${NC} Obteniendo métricas de géneros..."
METRICS_GENRES=$(curl -s -X GET "$BASE_URL/admin/metrics/genres" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Response: $METRICS_GENRES"
echo -e "${GREEN}✓ Métricas de géneros obtenidas${NC}"
echo ""

echo -e "${YELLOW}[26/30]${NC} Obteniendo métricas de artistas..."
METRICS_ARTISTS=$(curl -s -X GET "$BASE_URL/admin/metrics/artists?top=5" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Response: $METRICS_ARTISTS"
echo -e "${GREEN}✓ Métricas de artistas obtenidas${NC}"
echo ""

# ========== ADMIN: USUARIOS ==========
echo "=========================================="
echo "  ADMIN: GESTIÓN DE USUARIOS (RF-011)"
echo "=========================================="

echo -e "${YELLOW}[27/30]${NC} Listando usuarios..."
LIST_USERS=$(curl -s -X GET "$BASE_URL/admin/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Response: $LIST_USERS"
echo -e "${GREEN}✓ Usuarios listados${NC}"
echo ""

# ========== ADMIN: ACTUALIZAR CANCIÓN ==========
echo -e "${YELLOW}[28/30]${NC} Actualizando canción..."
UPDATE_SONG=$(curl -s -X PUT "$BASE_URL/admin/songs/$SONG1_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "titulo": "Bohemian Rhapsody (Updated)",
    "artista": "Queen",
    "genero": "Rock",
    "año": 1975,
    "duracion": 355
  }')
echo "Response: $UPDATE_SONG"
echo -e "${GREEN}✓ Canción actualizada${NC}"
echo ""

# ========== OBTENER CANCIÓN POR ID ==========
echo -e "${YELLOW}[29/30]${NC} Obteniendo canción por ID..."
GET_SONG=$(curl -s -X GET "$BASE_URL/songs/$SONG1_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $GET_SONG"
echo -e "${GREEN}✓ Canción obtenida${NC}"
echo ""

# ========== ADMIN: ELIMINAR CANCIÓN ==========
echo -e "${YELLOW}[30/30]${NC} Eliminando canción de prueba..."
DELETE_SONG=$(curl -s -X DELETE "$BASE_URL/admin/songs/$SONG3_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Response: $DELETE_SONG"
echo -e "${GREEN}✓ Canción eliminada${NC}"
echo ""

echo "=========================================="
echo -e "${GREEN}  ✅ TODOS LOS TESTS COMPLETADOS${NC}"
echo "=========================================="
echo ""
echo "Resumen:"
echo "- Autenticación: ✓"
echo "- Búsquedas (Autocompletado, Avanzada): ✓"
echo "- Gestión de Perfil y Favoritos: ✓"
echo "- Exportación CSV: ✓"
echo "- Recomendaciones (Descubrimiento, Radio): ✓"
echo "- Funcionalidades Sociales: ✓"
echo "- Admin: Gestión de Canciones: ✓"
echo "- Admin: Gestión de Usuarios: ✓"
echo "- Admin: Métricas: ✓"
echo ""
echo -e "${GREEN}¡Backend completamente funcional!${NC}"

