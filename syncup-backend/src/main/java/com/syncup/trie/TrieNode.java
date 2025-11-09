package com.syncup.trie;

import com.syncup.model.Cancion;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nodo del árbol Trie utilizado para autocompletado de búsquedas.
 * 
 * @author SyncUp Team
 */
@Data
public class TrieNode {
    
    /**
     * Mapa de caracteres a nodos hijos.
     * Permite navegación eficiente por el árbol.
     */
    private Map<Character, TrieNode> children;
    
    /**
     * Indica si este nodo representa el final de una palabra.
     */
    private boolean isEndOfWord;
    
    /**
     * Lista de canciones asociadas a este nodo.
     * Permite almacenar múltiples canciones con el mismo título.
     */
    private List<Cancion> canciones;
    
    /**
     * Constructor que inicializa el nodo.
     */
    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.canciones = new ArrayList<>();
    }
    
    /**
     * Verifica si el nodo tiene un hijo para el carácter dado.
     * 
     * @param c carácter a verificar
     * @return true si existe el hijo, false en caso contrario
     * Complejidad: O(1)
     */
    public boolean tieneHijo(char c) {
        return children.containsKey(c);
    }
    
    /**
     * Obtiene el nodo hijo para el carácter dado.
     * 
     * @param c carácter del hijo
     * @return nodo hijo o null si no existe
     * Complejidad: O(1)
     */
    public TrieNode obtenerHijo(char c) {
        return children.get(c);
    }
}

