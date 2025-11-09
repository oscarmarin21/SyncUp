package com.syncup.graph;

import com.syncup.model.Usuario;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Implementación de un Grafo No Dirigido para modelar conexiones sociales entre usuarios.
 * Requerido según RF-023 y RF-024.
 * 
 * En este grafo, si un usuario A sigue a un usuario B, existe una arista entre ambos.
 * Como es no dirigido, la relación es mutua: si A sigue a B, entonces B y A están conectados.
 * 
 * @author SyncUp Team
 */
@Slf4j
public class GrafoSocial {
    
    /**
     * Representación del grafo como mapa de adyacencia.
     * Key: Usuario, Value: Set de usuarios conectados (seguidos/seguidores)
     * Como es no dirigido, si A está conectado con B, entonces B está en el set de A y viceversa.
     * Complejidad de acceso: O(1)
     */
    private final Map<Usuario, Set<Usuario>> grafo;
    
    /**
     * Constructor que inicializa el grafo vacío.
     */
    public GrafoSocial() {
        this.grafo = new HashMap<>();
    }
    
    /**
     * Establece una conexión (sigue relación) entre dos usuarios.
     * Como el grafo es no dirigido, agrega la conexión en ambas direcciones.
     * Requerido según RF-007.
     * Complejidad: O(1)
     * 
     * @param seguidor usuario que sigue
     * @param seguido usuario que es seguido
     */
    public void seguir(Usuario seguidor, Usuario seguido) {
        if (seguidor == null || seguido == null || seguidor.equals(seguido)) {
            return;
        }
        
        // Agregar seguido a la lista de seguidos de seguidor
        grafo.computeIfAbsent(seguidor, k -> new HashSet<>()).add(seguido);
        
        // Agregar seguidor a la lista de seguidores de seguido (grafo no dirigido)
        grafo.computeIfAbsent(seguido, k -> new HashSet<>()).add(seguidor);
        
        log.debug("Usuario '{}' ahora sigue a '{}'", seguidor.getUsername(), seguido.getUsername());
    }
    
    /**
     * Elimina la conexión entre dos usuarios (dejar de seguir).
     * Complejidad: O(1)
     * 
     * @param seguidor usuario que deja de seguir
     * @param seguido usuario que ya no es seguido
     */
    public void dejarDeSeguir(Usuario seguidor, Usuario seguido) {
        if (seguidor == null || seguido == null) {
            return;
        }
        
        // Eliminar seguido de la lista de seguidor
        Set<Usuario> seguidos = grafo.get(seguidor);
        if (seguidos != null) {
            seguidos.remove(seguido);
            if (seguidos.isEmpty()) {
                grafo.remove(seguidor);
            }
        }
        
        // Eliminar seguidor de la lista de seguido (grafo no dirigido)
        Set<Usuario> seguidores = grafo.get(seguido);
        if (seguidores != null) {
            seguidores.remove(seguidor);
            if (seguidores.isEmpty()) {
                grafo.remove(seguido);
            }
        }
        
        log.debug("Usuario '{}' dejó de seguir a '{}'", seguidor.getUsername(), seguido.getUsername());
    }
    
    /**
     * Obtiene todos los usuarios seguidos por un usuario dado.
     * Complejidad: O(1) para obtener el set, O(n) para copiarlo donde n es el número de seguidos
     * 
     * @param usuario usuario del cual obtener seguidos
     * @return conjunto de usuarios seguidos
     */
    public Set<Usuario> obtenerSeguidos(Usuario usuario) {
        Set<Usuario> seguidos = grafo.get(usuario);
        return seguidos == null ? new HashSet<>() : new HashSet<>(seguidos);
    }
    
    /**
     * Obtiene todos los seguidores de un usuario dado.
     * En un grafo no dirigido, los seguidores son los mismos que los seguidos.
     * Complejidad: O(1) para obtener el set, O(n) para copiarlo
     * 
     * @param usuario usuario del cual obtener seguidores
     * @return conjunto de usuarios seguidores
     */
    public Set<Usuario> obtenerSeguidores(Usuario usuario) {
        // En un grafo no dirigido, los seguidores son los mismos que los seguidos
        return obtenerSeguidos(usuario);
    }
    
    /**
     * Verifica si un usuario sigue a otro.
     * Complejidad: O(1)
     * 
     * @param seguidor usuario que podría estar siguiendo
     * @param seguido usuario que podría ser seguido
     * @return true si existe la conexión, false en caso contrario
     */
    public boolean sigueA(Usuario seguidor, Usuario seguido) {
        Set<Usuario> seguidos = grafo.get(seguidor);
        return seguidos != null && seguidos.contains(seguido);
    }
    
    /**
     * Obtiene el número de conexiones (grado) de un usuario.
     * Complejidad: O(1)
     * 
     * @param usuario usuario del cual obtener el grado
     * @return número de conexiones
     */
    public int obtenerGrado(Usuario usuario) {
        Set<Usuario> conexiones = grafo.get(usuario);
        return conexiones == null ? 0 : conexiones.size();
    }
    
    /**
     * Obtiene todos los usuarios (nodos) del grafo.
     * 
     * @return conjunto de usuarios en el grafo
     */
    public Set<Usuario> obtenerNodos() {
        return new HashSet<>(grafo.keySet());
    }
    
    /**
     * Obtiene el número de nodos en el grafo.
     * 
     * @return número de usuarios en el grafo
     */
    public int obtenerNumeroNodos() {
        return grafo.size();
    }
    
    /**
     * Verifica si el grafo está vacío.
     * 
     * @return true si está vacío, false en caso contrario
     */
    public boolean estaVacio() {
        return grafo.isEmpty();
    }
    
    /**
     * Limpia el grafo completamente.
     */
    public void limpiar() {
        grafo.clear();
        log.info("Grafo social limpiado");
    }
}

