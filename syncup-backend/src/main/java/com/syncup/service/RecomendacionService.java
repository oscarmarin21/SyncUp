package com.syncup.service;

import com.syncup.graph.GrafoDeSimilitud;
import com.syncup.graph.algoritmo.Dijkstra;
import com.syncup.model.Cancion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que genera recomendaciones musicales para los usuarios.
 * Requerido según RF-005 y RF-006.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecomendacionService {
    
    private final com.syncup.service.FavoritosService favoritosService;
    private final com.syncup.service.SimilitudService similitudService;
    
    /**
     * Genera una playlist de "Descubrimiento Semanal" basada en los gustos del usuario.
     * Requerido según RF-005.
     * 
     * Utiliza los favoritos del usuario y el grafo de similitud con Dijkstra para encontrar
     * canciones similares pero diferentes.
     * 
     * @param username username del usuario
     * @param maxCanciones número máximo de canciones en la playlist
     * @return lista de canciones recomendadas
     */
    public List<Cancion> generarDescubrimientoSemanal(String username, int maxCanciones) {
        log.debug("Generando descubrimiento semanal para usuario: {}", username);
        
        // Obtener favoritos del usuario
        LinkedList<Cancion> favoritos = favoritosService.obtenerFavoritos(username);
        
        if (favoritos.isEmpty()) {
            log.debug("Usuario '{}' no tiene favoritos, retornando lista vacía", username);
            return Collections.emptyList();
        }
        
        GrafoDeSimilitud grafo = similitudService.obtenerGrafo();
        Set<Cancion> recomendaciones = new LinkedHashSet<>();
        
        // Para cada canción favorita, encontrar canciones similares
        for (Cancion favorito : favoritos) {
            // Usar Dijkstra para encontrar canciones conectadas con mayor similitud
            List<Cancion> similares = Dijkstra.encontrarSimilares(grafo, favorito, 5);
            
            // Filtrar canciones que ya están en favoritos
            similares.stream()
                    .filter(c -> !favoritos.contains(c))
                    .limit(maxCanciones - recomendaciones.size())
                    .forEach(recomendaciones::add);
            
            if (recomendaciones.size() >= maxCanciones) {
                break;
            }
        }
        
        List<Cancion> resultado = new ArrayList<>(recomendaciones);
        log.debug("Descubrimiento semanal generado: {} canciones para usuario '{}'", 
                resultado.size(), username);
        
        return resultado.stream()
                .limit(maxCanciones)
                .collect(Collectors.toList());
    }
    
    /**
     * Inicia una "Radio" a partir de una canción semilla.
     * Requerido según RF-006.
     * 
     * Genera una cola de reproducción con canciones similares, usando el grafo de similitud.
     * 
     * @param cancionSemilla canción desde la cual iniciar la radio
     * @param maxCanciones número máximo de canciones en la cola
     * @return lista de canciones para la radio
     */
    public List<Cancion> iniciarRadio(Cancion cancionSemilla, int maxCanciones) {
        if (cancionSemilla == null) {
            return Collections.emptyList();
        }
        
        log.debug("Iniciando radio desde canción: {}", cancionSemilla.getTitulo());
        
        GrafoDeSimilitud grafo = similitudService.obtenerGrafo();
        List<Cancion> radio = new ArrayList<>();
        radio.add(cancionSemilla); // Agregar la canción semilla al inicio
        
        // Usar algoritmo para encontrar canciones similares
        List<Cancion> similares = Dijkstra.encontrarSimilares(grafo, cancionSemilla, maxCanciones - 1);
        
        // Agregar canciones similares a la radio
        for (Cancion similar : similares) {
            if (!similar.equals(cancionSemilla) && radio.size() < maxCanciones) {
                radio.add(similar);
            }
        }
        
        log.debug("Radio generada con {} canciones", radio.size());
        return radio;
    }
}

