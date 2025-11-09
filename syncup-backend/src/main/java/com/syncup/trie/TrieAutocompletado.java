package com.syncup.trie;

import com.syncup.model.Cancion;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementación de un Trie (Árbol de Prefijos) para búsqueda eficiente de canciones.
 * Requerido según RF-025 y RF-026.
 * 
 * @author SyncUp Team
 */
@Slf4j
public class TrieAutocompletado {
    
    /**
     * Nodo raíz del árbol Trie.
     */
    private final TrieNode raiz;
    
    /**
     * Constructor que inicializa el Trie con un nodo raíz vacío.
     */
    public TrieAutocompletado() {
        this.raiz = new TrieNode();
    }
    
    /**
     * Inserta una canción en el Trie basándose en su título.
     * El título se convierte a minúsculas para búsquedas case-insensitive.
     * Complejidad: O(m) donde m es la longitud del título
     * 
     * @param titulo título de la canción a insertar
     * @param cancion canción asociada al título
     */
    public void insertar(String titulo, Cancion cancion) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return;
        }
        
        TrieNode actual = raiz;
        String tituloLower = titulo.toLowerCase().trim();
        
        // Recorrer cada carácter del título
        for (char c : tituloLower.toCharArray()) {
            // Si el nodo actual no tiene hijo para este carácter, crearlo
            if (!actual.tieneHijo(c)) {
                actual.getChildren().put(c, new TrieNode());
            }
            // Avanzar al nodo hijo
            actual = actual.obtenerHijo(c);
        }
        
        // Marcar el nodo final como fin de palabra y agregar la canción
        actual.setEndOfWord(true);
        actual.getCanciones().add(cancion);
        
        log.debug("Canción '{}' insertada en Trie", titulo);
    }
    
    /**
     * Busca todas las canciones cuyos títulos comienzan con el prefijo dado.
     * Requerido según RF-026.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k es el número de canciones encontradas
     * 
     * @param prefix prefijo de búsqueda
     * @return lista de canciones cuyos títulos empiezan con el prefijo
     */
    public List<Cancion> buscarPorPrefijo(String prefix) {
        List<Cancion> resultados = new ArrayList<>();
        
        if (prefix == null || prefix.trim().isEmpty()) {
            return resultados;
        }
        
        String prefixLower = prefix.toLowerCase().trim();
        TrieNode actual = raiz;
        
        // Navegar hasta el nodo correspondiente al prefijo
        for (char c : prefixLower.toCharArray()) {
            if (!actual.tieneHijo(c)) {
                // Prefijo no encontrado
                return resultados;
            }
            actual = actual.obtenerHijo(c);
        }
        
        // Una vez en el nodo del prefijo, recolectar todas las canciones
        // desde este nodo hacia abajo (DFS)
        Set<Cancion> cancionesUnicas = new HashSet<>();
        recolectarCanciones(actual, cancionesUnicas);
        
        resultados.addAll(cancionesUnicas);
        log.debug("Búsqueda por prefijo '{}' encontró {} canciones", prefix, resultados.size());
        
        return resultados;
    }
    
    /**
     * Recolecta todas las canciones desde un nodo hacia abajo usando DFS.
     * 
     * @param nodo nodo desde donde empezar la búsqueda
     * @param canciones conjunto donde se almacenan las canciones encontradas
     */
    private void recolectarCanciones(TrieNode nodo, Set<Cancion> canciones) {
        if (nodo == null) {
            return;
        }
        
        // Agregar canciones del nodo actual
        if (nodo.isEndOfWord()) {
            canciones.addAll(nodo.getCanciones());
        }
        
        // Recorrer recursivamente todos los hijos
        for (TrieNode hijo : nodo.getChildren().values()) {
            recolectarCanciones(hijo, canciones);
        }
    }
    
    /**
     * Elimina una canción del Trie.
     * 
     * @param titulo título de la canción a eliminar
     * @param cancion canción a eliminar
     * @return true si se eliminó, false en caso contrario
     */
    public boolean eliminar(String titulo, Cancion cancion) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return false;
        }
        
        String tituloLower = titulo.toLowerCase().trim();
        TrieNode actual = raiz;
        
        // Navegar hasta el nodo correspondiente al título completo
        for (char c : tituloLower.toCharArray()) {
            if (!actual.tieneHijo(c)) {
                return false;
            }
            actual = actual.obtenerHijo(c);
        }
        
        // Eliminar la canción del nodo final
        boolean eliminado = actual.getCanciones().remove(cancion);
        
        // Si no hay más canciones, marcar como no fin de palabra
        if (actual.getCanciones().isEmpty()) {
            actual.setEndOfWord(false);
        }
        
        return eliminado;
    }
    
    /**
     * Verifica si el Trie está vacío.
     * 
     * @return true si está vacío, false en caso contrario
     * Complejidad: O(1)
     */
    public boolean estaVacio() {
        return raiz.getChildren().isEmpty();
    }

    /**
     * Limpia el Trie eliminando todos los nodos y canciones almacenadas.
     * Complejidad: O(n) donde n es el número de nodos en el Trie.
     */
    public void limpiar() {
        raiz.getChildren().clear();
        raiz.getCanciones().clear();
        raiz.setEndOfWord(false);
    }
}

