package com.syncup.graph;

import com.syncup.model.Cancion;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Implementación de un Grafo Ponderado No Dirigido para modelar similitudes entre canciones.
 * Requerido según RF-021 y RF-022.
 * 
 * Cada arista tiene un peso que representa el grado de similitud entre dos canciones.
 * Mayor peso = mayor similitud.
 * 
 * @author SyncUp Team
 */
@Slf4j
public class GrafoDeSimilitud {
    
    /**
     * Representación del grafo como mapa de adyacencia.
     * Key: Cancion origen, Value: Map<Cancion destino, Peso de la arista>
     * Como es no dirigido, si existe (A -> B con peso w), también existe (B -> A con peso w).
     * Complejidad de acceso: O(1)
     */
    private final Map<Cancion, Map<Cancion, Double>> grafo;
    
    /**
     * Constructor que inicializa el grafo vacío.
     */
    public GrafoDeSimilitud() {
        this.grafo = new HashMap<>();
    }
    
    /**
     * Agrega una arista ponderada entre dos canciones.
     * Como el grafo es no dirigido, agrega la arista en ambas direcciones.
     * Complejidad: O(1)
     * 
     * @param origen canción origen
     * @param destino canción destino
     * @param peso peso de la arista (grado de similitud)
     */
    public void agregarArista(Cancion origen, Cancion destino, double peso) {
        if (origen == null || destino == null || origen.equals(destino)) {
            return;
        }
        
        // Agregar arista en dirección origen -> destino
        grafo.computeIfAbsent(origen, k -> new HashMap<>()).put(destino, peso);
        
        // Agregar arista en dirección destino -> origen (grafo no dirigido)
        grafo.computeIfAbsent(destino, k -> new HashMap<>()).put(origen, peso);
        
        log.debug("Arista agregada: {} -> {} con peso {}", 
                origen.getTitulo(), destino.getTitulo(), peso);
    }
    
    /**
     * Obtiene los vecinos (canciones adyacentes) de una canción.
     * Complejidad: O(1) para obtener el mapa, O(v) para construir la lista donde v es el número de vecinos
     * 
     * @param cancion canción de la cual obtener vecinos
     * @return lista de canciones vecinas
     */
    public List<Cancion> obtenerVecinos(Cancion cancion) {
        Map<Cancion, Double> vecinos = grafo.get(cancion);
        if (vecinos == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(vecinos.keySet());
    }
    
    /**
     * Obtiene el peso de la arista entre dos canciones.
     * Complejidad: O(1)
     * 
     * @param origen canción origen
     * @param destino canción destino
     * @return peso de la arista o null si no existe
     */
    public Double obtenerPeso(Cancion origen, Cancion destino) {
        Map<Cancion, Double> vecinos = grafo.get(origen);
        if (vecinos == null) {
            return null;
        }
        return vecinos.get(destino);
    }
    
    /**
     * Calcula el grado de similitud entre dos canciones.
     * La similitud se basa en:
     * - Mismo género: +0.5
     * - Mismo artista: +0.4
     * - Proximidad de años (diferencia máxima 5 años): +0.3 * (1 - diferencia/5)
     * 
     * Complejidad: O(1)
     * 
     * @param c1 primera canción
     * @param c2 segunda canción
     * @return grado de similitud entre 0.0 y 1.0
     */
    public double calcularSimilitud(Cancion c1, Cancion c2) {
        if (c1 == null || c2 == null || c1.equals(c2)) {
            return 1.0; // Misma canción
        }
        
        double similitud = 0.0;
        
        // Similitud por género
        if (c1.getGenero().equalsIgnoreCase(c2.getGenero())) {
            similitud += 0.5;
        }
        
        // Similitud por artista
        if (c1.getArtista().equalsIgnoreCase(c2.getArtista())) {
            similitud += 0.4;
        }
        
        // Similitud por año (proximidad)
        int diferenciaAnos = Math.abs(c1.getAño() - c2.getAño());
        if (diferenciaAnos <= 5) {
            similitud += 0.3 * (1.0 - diferenciaAnos / 5.0);
        }
        
        return Math.min(similitud, 1.0); // Normalizar a máximo 1.0
    }
    
    /**
     * Construye el grafo calculando similitudes entre todas las canciones.
     * Solo agrega aristas con similitud mayor a un umbral (0.3).
     * 
     * @param canciones lista de todas las canciones en el sistema
     */
    public void construirGrafo(List<Cancion> canciones) {
        log.info("Construyendo grafo de similitud con {} canciones...", canciones.size());
        grafo.clear();
        
        int aristasAgregadas = 0;
        for (int i = 0; i < canciones.size(); i++) {
            for (int j = i + 1; j < canciones.size(); j++) {
                Cancion c1 = canciones.get(i);
                Cancion c2 = canciones.get(j);
                
                double similitud = calcularSimilitud(c1, c2);
                
                // Solo agregar arista si la similitud supera un umbral
                if (similitud >= 0.3) {
                    agregarArista(c1, c2, similitud);
                    aristasAgregadas++;
                }
            }
        }
        
        log.info("Grafo construido con {} nodos y {} aristas", 
                grafo.size(), aristasAgregadas);
    }
    
    /**
     * Verifica si existe una arista entre dos canciones.
     * Complejidad: O(1)
     * 
     * @param origen canción origen
     * @param destino canción destino
     * @return true si existe la arista, false en caso contrario
     */
    public boolean existeArista(Cancion origen, Cancion destino) {
        Map<Cancion, Double> vecinos = grafo.get(origen);
        return vecinos != null && vecinos.containsKey(destino);
    }
    
    /**
     * Obtiene todas las canciones (nodos) del grafo.
     * 
     * @return conjunto de canciones en el grafo
     */
    public Set<Cancion> obtenerNodos() {
        return new HashSet<>(grafo.keySet());
    }
    
    /**
     * Obtiene el número de nodos en el grafo.
     * 
     * @return número de nodos
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
}

