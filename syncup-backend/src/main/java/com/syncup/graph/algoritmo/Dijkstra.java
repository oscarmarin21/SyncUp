package com.syncup.graph.algoritmo;

import com.syncup.graph.GrafoDeSimilitud;
import com.syncup.model.Cancion;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Implementación del algoritmo de Dijkstra para encontrar el camino de menor costo
 * (mayor similitud) entre dos canciones en el grafo de similitud.
 * Requerido según RF-022.
 * 
 * En este contexto, "menor costo" significa mayor similitud (peso más alto).
 * El algoritmo busca maximizar la similitud acumulada en el camino.
 * 
 * Complejidad: O(V log V + E log V) donde V es el número de vértices y E el número de aristas
 * 
 * @author SyncUp Team
 */
@Slf4j
public class Dijkstra {
    
    /**
     * Encuentra el camino de mayor similitud (menor costo invertido) entre dos canciones.
     * 
     * @param grafo grafo de similitud
     * @param origen canción origen
     * @param destino canción destino
     * @return lista de canciones que forman el camino de mayor similitud, o lista vacía si no hay camino
     */
    public static List<Cancion> encontrarCamino(GrafoDeSimilitud grafo, 
                                                Cancion origen, 
                                                Cancion destino) {
        if (grafo == null || origen == null || destino == null) {
            return new ArrayList<>();
        }
        
        if (origen.equals(destino)) {
            return List.of(origen);
        }
        
        // Mapa de distancias (similitud acumulada): queremos maximizar, así que usamos inverso
        Map<Cancion, Double> distancia = new HashMap<>();
        Map<Cancion, Cancion> predecesor = new HashMap<>();
        Set<Cancion> visitados = new HashSet<>();
        
        // PriorityQueue para seleccionar el nodo con mayor similitud acumulada
        // Usamos comparador inverso porque queremos maximizar similitud (que es el peso)
        PriorityQueue<NodoDijkstra> cola = new PriorityQueue<>(
            (n1, n2) -> Double.compare(n2.getDistancia(), n1.getDistancia())
        );
        
        // Inicializar distancias: origen con distancia 0 (o 1.0 para maximizar similitud)
        distancia.put(origen, 0.0);
        cola.offer(new NodoDijkstra(origen, 0.0));
        
        while (!cola.isEmpty()) {
            NodoDijkstra actual = cola.poll();
            Cancion cancionActual = actual.getCancion();
            
            if (visitados.contains(cancionActual)) {
                continue;
            }
            
            visitados.add(cancionActual);
            
            // Si llegamos al destino, reconstruir camino
            if (cancionActual.equals(destino)) {
                return reconstruirCamino(predecesor, origen, destino);
            }
            
            // Explorar vecinos
            List<Cancion> vecinos = grafo.obtenerVecinos(cancionActual);
            for (Cancion vecino : vecinos) {
                if (visitados.contains(vecino)) {
                    continue;
                }
                
                Double peso = grafo.obtenerPeso(cancionActual, vecino);
                if (peso == null) {
                    continue;
                }
                
                // En lugar de sumar, restamos (porque queremos maximizar similitud)
                // Usamos 1 - peso para convertir similitud en "costo"
                double costoArista = 1.0 - peso; // Invertir: mayor similitud = menor costo
                double distanciaActual = distancia.getOrDefault(cancionActual, Double.MAX_VALUE);
                double nuevaDistancia = distanciaActual + costoArista;
                
                double distanciaVecino = distancia.getOrDefault(vecino, Double.MAX_VALUE);
                
                if (nuevaDistancia < distanciaVecino) {
                    distancia.put(vecino, nuevaDistancia);
                    predecesor.put(vecino, cancionActual);
                    cola.offer(new NodoDijkstra(vecino, nuevaDistancia));
                }
            }
        }
        
        // No se encontró camino
        log.debug("No se encontró camino entre {} y {}", origen.getTitulo(), destino.getTitulo());
        return new ArrayList<>();
    }
    
    /**
     * Encuentra canciones similares a una canción origen, explorando hasta cierta distancia.
     * Útil para generar recomendaciones basadas en similitud.
     * 
     * @param grafo grafo de similitud
     * @param origen canción origen
     * @param maxCanciones número máximo de canciones similares a retornar
     * @return lista de canciones similares ordenadas por similitud descendente
     */
    public static List<Cancion> encontrarSimilares(GrafoDeSimilitud grafo, 
                                                   Cancion origen, 
                                                   int maxCanciones) {
        if (grafo == null || origen == null) {
            return new ArrayList<>();
        }
        
        List<Cancion> vecinos = grafo.obtenerVecinos(origen);
        
        // Ordenar vecinos por peso (similitud) descendente
        List<ParCancionSimilitud> cancionesConSimilitud = new ArrayList<>();
        for (Cancion vecino : vecinos) {
            Double peso = grafo.obtenerPeso(origen, vecino);
            if (peso != null) {
                cancionesConSimilitud.add(new ParCancionSimilitud(vecino, peso));
            }
        }
        
        cancionesConSimilitud.sort((p1, p2) -> Double.compare(p2.getSimilitud(), p1.getSimilitud()));
        
        // Retornar las top N
        int limite = Math.min(maxCanciones, cancionesConSimilitud.size());
        List<Cancion> resultado = new ArrayList<>();
        for (int i = 0; i < limite; i++) {
            resultado.add(cancionesConSimilitud.get(i).getCancion());
        }
        
        return resultado;
    }
    
    /**
     * Reconstruye el camino desde el destino hasta el origen usando el mapa de predecesores.
     * 
     * @param predecesor mapa de predecesores
     * @param origen canción origen
     * @param destino canción destino
     * @return lista de canciones que forman el camino
     */
    private static List<Cancion> reconstruirCamino(Map<Cancion, Cancion> predecesor,
                                                   Cancion origen,
                                                   Cancion destino) {
        List<Cancion> camino = new ArrayList<>();
        Cancion actual = destino;
        
        while (actual != null) {
            camino.add(0, actual); // Agregar al inicio para mantener orden
            actual = predecesor.get(actual);
            
            if (actual != null && actual.equals(origen)) {
                camino.add(0, origen);
                break;
            }
        }
        
        return camino;
    }
    
    /**
     * Clase auxiliar para el PriorityQueue de Dijkstra.
     */
    private static class NodoDijkstra {
        private final Cancion cancion;
        private final double distancia;
        
        public NodoDijkstra(Cancion cancion, double distancia) {
            this.cancion = cancion;
            this.distancia = distancia;
        }
        
        public Cancion getCancion() {
            return cancion;
        }
        
        public double getDistancia() {
            return distancia;
        }
    }
    
    /**
     * Clase auxiliar para almacenar canciones con su similitud.
     */
    private static class ParCancionSimilitud {
        private final Cancion cancion;
        private final double similitud;
        
        public ParCancionSimilitud(Cancion cancion, double similitud) {
            this.cancion = cancion;
            this.similitud = similitud;
        }
        
        public Cancion getCancion() {
            return cancion;
        }
        
        public double getSimilitud() {
            return similitud;
        }
    }
}

