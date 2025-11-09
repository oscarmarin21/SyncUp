package com.syncup.graph.algoritmo;

import com.syncup.graph.GrafoSocial;
import com.syncup.model.Usuario;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Implementación del algoritmo BFS (Breadth-First Search) para recorrer el grafo social.
 * Requerido según RF-024.
 * 
 * Útil para encontrar "amigos de amigos" y sugerir usuarios a seguir.
 * 
 * Complejidad: O(V + E) donde V es el número de vértices y E el número de aristas
 * 
 * @author SyncUp Team
 */
@Slf4j
public class BFS {
    
    /**
     * Encuentra usuarios sugeridos basándose en las conexiones sociales.
     * Explora el grafo usando BFS hasta una profundidad máxima.
     * Requerido según RF-008.
     * 
     * @param grafo grafo social
     * @param origen usuario origen desde donde empezar la búsqueda
     * @param profundidadMax profundidad máxima de exploración (1 = solo amigos directos, 2 = amigos de amigos, etc.)
     * @param maxUsuarios número máximo de usuarios sugeridos a retornar
     * @return lista de usuarios sugeridos
     */
    public static List<Usuario> encontrarUsuariosSugeridos(GrafoSocial grafo,
                                                           Usuario origen,
                                                           int profundidadMax,
                                                           int maxUsuarios) {
        if (grafo == null || origen == null || profundidadMax < 1) {
            return new ArrayList<>();
        }
        
        List<Usuario> sugeridos = new ArrayList<>();
        Set<Usuario> visitados = new HashSet<>();
        Set<Usuario> yaSeguidos = grafo.obtenerSeguidos(origen); // No sugerir usuarios que ya sigue
        visitados.add(origen); // No sugerir al propio usuario
        visitados.addAll(yaSeguidos); // No sugerir usuarios que ya sigue
        
        // Cola para BFS: almacena (usuario, profundidad)
        Queue<ParUsuarioProfundidad> cola = new LinkedList<>();
        
        // Inicializar con los vecinos directos (amigos) del usuario origen
        Set<Usuario> amigosDirectos = grafo.obtenerSeguidos(origen);
        for (Usuario amigo : amigosDirectos) {
            cola.offer(new ParUsuarioProfundidad(amigo, 1));
            visitados.add(amigo);
        }
        
        // BFS hasta la profundidad máxima
        while (!cola.isEmpty() && sugeridos.size() < maxUsuarios) {
            ParUsuarioProfundidad actual = cola.poll();
            Usuario usuarioActual = actual.getUsuario();
            int profundidadActual = actual.getProfundidad();
            
            // Solo sugerir usuarios que no se siguen ya (amigos de amigos)
            // Los amigos directos (profundidad 1) ya están en yaSeguidos, así que se saltan
            // Solo sugerimos a partir de profundidad 2 (amigos de amigos)
            if (profundidadActual > 1 && !yaSeguidos.contains(usuarioActual) && !usuarioActual.equals(origen)) {
                sugeridos.add(usuarioActual);
            }
            
            // Si no hemos alcanzado la profundidad máxima, explorar vecinos
            if (profundidadActual < profundidadMax) {
                Set<Usuario> vecinos = grafo.obtenerSeguidos(usuarioActual);
                for (Usuario vecino : vecinos) {
                    if (!visitados.contains(vecino)) {
                        visitados.add(vecino);
                        cola.offer(new ParUsuarioProfundidad(vecino, profundidadActual + 1));
                    }
                }
            }
        }
        
        log.debug("BFS encontró {} usuarios sugeridos para '{}'", 
                sugeridos.size(), origen.getUsername());
        
        return sugeridos;
    }
    
    /**
     * Encuentra todos los usuarios alcanzables desde un usuario origen hasta cierta profundidad.
     * 
     * @param grafo grafo social
     * @param origen usuario origen
     * @param profundidadMax profundidad máxima
     * @return conjunto de usuarios alcanzables
     */
    public static Set<Usuario> encontrarUsuariosAlcanzables(GrafoSocial grafo,
                                                            Usuario origen,
                                                            int profundidadMax) {
        if (grafo == null || origen == null) {
            return new HashSet<>();
        }
        
        Set<Usuario> alcanzables = new HashSet<>();
        Set<Usuario> visitados = new HashSet<>();
        Queue<ParUsuarioProfundidad> cola = new LinkedList<>();
        
        visitados.add(origen);
        cola.offer(new ParUsuarioProfundidad(origen, 0));
        
        while (!cola.isEmpty()) {
            ParUsuarioProfundidad actual = cola.poll();
            Usuario usuarioActual = actual.getUsuario();
            int profundidadActual = actual.getProfundidad();
            
            if (profundidadActual > 0) { // No incluir al usuario origen
                alcanzables.add(usuarioActual);
            }
            
            if (profundidadActual < profundidadMax) {
                Set<Usuario> vecinos = grafo.obtenerSeguidos(usuarioActual);
                for (Usuario vecino : vecinos) {
                    if (!visitados.contains(vecino)) {
                        visitados.add(vecino);
                        cola.offer(new ParUsuarioProfundidad(vecino, profundidadActual + 1));
                    }
                }
            }
        }
        
        return alcanzables;
    }
    
    /**
     * Verifica si dos usuarios están conectados (existe un camino entre ellos).
     * 
     * @param grafo grafo social
     * @param origen usuario origen
     * @param destino usuario destino
     * @param profundidadMax profundidad máxima de búsqueda
     * @return true si están conectados, false en caso contrario
     */
    public static boolean estanConectados(GrafoSocial grafo,
                                          Usuario origen,
                                          Usuario destino,
                                          int profundidadMax) {
        Set<Usuario> alcanzables = encontrarUsuariosAlcanzables(grafo, origen, profundidadMax);
        return alcanzables.contains(destino);
    }
    
    /**
     * Clase auxiliar para almacenar un usuario con su profundidad en el BFS.
     */
    private static class ParUsuarioProfundidad {
        private final Usuario usuario;
        private final int profundidad;
        
        public ParUsuarioProfundidad(Usuario usuario, int profundidad) {
            this.usuario = usuario;
            this.profundidad = profundidad;
        }
        
        public Usuario getUsuario() {
            return usuario;
        }
        
        public int getProfundidad() {
            return profundidad;
        }
    }
}

