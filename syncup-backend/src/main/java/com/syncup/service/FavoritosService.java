package com.syncup.service;

import com.syncup.model.Cancion;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio que gestiona las listas de favoritos de usuarios usando LinkedList.
 * Requerido según RF-015.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoritosService {
    
    /**
     * HashMap que almacena las listas de favoritos por usuario.
     * Key: username, Value: LinkedList de canciones favoritas.
     * Requerido según RF-015 para usar LinkedList.
     */
    private final Map<String, LinkedList<Cancion>> favoritosPorUsuario = new HashMap<>();
    
    /**
     * Inicializa las listas de favoritos vacías al arranque.
     * En una implementación completa, se cargarían desde la BD.
     */
    @PostConstruct
    public void inicializar() {
        log.info("FavoritosService inicializado");
    }
    
    /**
     * Agrega una canción a la lista de favoritos de un usuario.
     * Si el usuario no tiene lista, crea una nueva LinkedList.
     * Complejidad: O(1) para agregar al final de LinkedList
     * 
     * @param username nombre del usuario
     * @param cancion canción a agregar a favoritos
     * @return true si se agregó, false si ya existía
     */
    public boolean agregarFavorito(String username, Cancion cancion) {
        LinkedList<Cancion> favoritos = favoritosPorUsuario.computeIfAbsent(username, k -> new LinkedList<>());
        
        // Evitar duplicados
        if (favoritos.contains(cancion)) {
            return false;
        }
        
        favoritos.add(cancion);
        log.debug("Canción '{}' agregada a favoritos de usuario '{}'", cancion.getTitulo(), username);
        return true;
    }
    
    /**
     * Obtiene la lista de canciones favoritas de un usuario.
     * Retorna una copia para evitar modificaciones externas.
     * Complejidad: O(n) donde n es el número de favoritos
     * 
     * @param username nombre del usuario
     * @return LinkedList de canciones favoritas (puede estar vacía)
     */
    public LinkedList<Cancion> obtenerFavoritos(String username) {
        return new LinkedList<>(favoritosPorUsuario.getOrDefault(username, new LinkedList<>()));
    }
    
    /**
     * Elimina una canción de la lista de favoritos de un usuario.
     * Complejidad: O(n) donde n es el número de favoritos (búsqueda lineal en LinkedList)
     * 
     * @param username nombre del usuario
     * @param cancion canción a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminarFavorito(String username, Cancion cancion) {
        LinkedList<Cancion> favoritos = favoritosPorUsuario.get(username);
        if (favoritos == null || favoritos.isEmpty()) {
            return false;
        }
        
        boolean eliminado = favoritos.remove(cancion);
        if (eliminado) {
            log.debug("Canción '{}' eliminada de favoritos de usuario '{}'", cancion.getTitulo(), username);
            // Si la lista queda vacía, opcionalmente eliminarla del mapa
            if (favoritos.isEmpty()) {
                favoritosPorUsuario.remove(username);
            }
        }
        return eliminado;
    }
    
    /**
     * Verifica si una canción está en los favoritos de un usuario.
     * Complejidad: O(n) donde n es el número de favoritos
     * 
     * @param username nombre del usuario
     * @param cancion canción a verificar
     * @return true si está en favoritos, false en caso contrario
     */
    public boolean esFavorito(String username, Cancion cancion) {
        LinkedList<Cancion> favoritos = favoritosPorUsuario.get(username);
        return favoritos != null && favoritos.contains(cancion);
    }
    
    /**
     * Obtiene el número de favoritos de un usuario.
     * Complejidad: O(1)
     * 
     * @param username nombre del usuario
     * @return número de favoritos
     */
    public int contarFavoritos(String username) {
        LinkedList<Cancion> favoritos = favoritosPorUsuario.get(username);
        return favoritos == null ? 0 : favoritos.size();
    }
}

