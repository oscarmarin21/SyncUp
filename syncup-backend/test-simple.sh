#!/bin/bash

# Test simple para verificar endpoints básicos

BASE_URL="http://localhost:8080/api"

echo "Testing endpoints básicos..."
echo ""

# 1. Login admin
echo "[1] Login como admin..."
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token obtenido: ${TOKEN:0:30}..."
echo ""

# 2. Crear canción
echo "[2] Creando canción..."
SONG_RESPONSE=$(curl -s -X POST "$BASE_URL/admin/songs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "titulo": "Test Song",
    "artista": "Test Artist",
    "genero": "Test",
    "año": 2024,
    "duracion": 180
  }')
echo "Response: $SONG_RESPONSE"
echo ""

echo "Test completado!"

