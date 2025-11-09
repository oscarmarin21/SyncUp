package com.syncup.service;

import com.syncup.graph.GrafoDeSimilitud;
import com.syncup.model.Cancion;
import com.syncup.repository.CancionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que gestiona el Grafo de Similitud entre canciones.
 * Requerido según RF-021 y RF-022.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimilitudService {
    
    private final CancionRepository cancionRepository;
    
    /**
     * Instancia del grafo de similitud.
     */
    private final GrafoDeSimilitud grafoDeSimilitud = new GrafoDeSimilitud();
    
    /**
     * Construye el grafo de similitud cargando todas las canciones desde la BD.
     * Se ejecuta al arranque de la aplicación.
     */
    @PostConstruct
    public void construirGrafo() {
        log.info("Construyendo grafo de similitud...");
        List<Cancion> canciones = cancionRepository.findAll();
        grafoDeSimilitud.construirGrafo(canciones);
        log.info("Grafo de similitud construido exitosamente");
    }
    
    /**
     * Obtiene la instancia del grafo de similitud.
     * 
     * @return grafo de similitud
     */
    public GrafoDeSimilitud obtenerGrafo() {
        return grafoDeSimilitud;
    }
    
    /**
     * Obtiene canciones similares a una canción dada.
     * 
     * @param cancion canción de referencia
     * @return lista de canciones vecinas (similares)
     */
    public List<Cancion> obtenerSimilares(Cancion cancion) {
        return grafoDeSimilitud.obtenerVecinos(cancion);
    }
    
    /**
     * Reconstruye el grafo de similitud.
     * Útil cuando se agregan o eliminan canciones.
     */
    public void reconstruirGrafo() {
        construirGrafo();
    }
    
    /**
     * Agrega una nueva canción al grafo de similitud.
     * Calcula similitudes con todas las canciones existentes.
     * 
     * @param nuevaCancion canción a agregar
     */
    public void agregarCancion(Cancion nuevaCancion) {
        List<Cancion> cancionesExistentes = cancionRepository.findAll();
        
        for (Cancion cancionExistente : cancionesExistentes) {
            if (!nuevaCancion.equals(cancionExistente)) {
                double similitud = grafoDeSimilitud.calcularSimilitud(nuevaCancion, cancionExistente);
                
                if (similitud >= 0.3) {
                    grafoDeSimilitud.agregarArista(nuevaCancion, cancionExistente, similitud);
                }
            }
        }
        
        log.debug("Canción '{}' agregada al grafo de similitud", nuevaCancion.getTitulo());
    }
    
    /**
     * Elimina una canción del grafo de similitud.
     * Nota: El grafo se reconstruirá en el siguiente arranque o manualmente.
     * 
     * @param cancion canción a eliminar
     */
    public void eliminarCancion(Cancion cancion) {
        // Para eliminar completamente, sería necesario reconstruir el grafo
        // Por simplicidad, se reconstruirá en el siguiente arranque
        log.debug("Canción '{}' será eliminada del grafo en la próxima reconstrucción", cancion.getTitulo());
    }
}

