package com.syncup.service;

import com.syncup.dto.SearchRequest;
import com.syncup.model.Cancion;
import com.syncup.repository.CancionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio que implementa búsqueda avanzada de canciones con concurrencia.
 * Requerido según RF-004 y RF-030.
 * 
 * Utiliza hilos de ejecución para buscar por diferentes atributos en paralelo.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusquedaAvanzadaService {
    
    private final CancionRepository cancionRepository;
    
    /**
     * Realiza una búsqueda avanzada de canciones con múltiples criterios.
     * Las búsquedas por cada atributo se ejecutan en paralelo usando CompletableFuture.
     * Requerido según RF-004 (búsqueda avanzada) y RF-030 (concurrencia).
     * 
     * Complejidad: O(n) por cada criterio, ejecutado en paralelo
     * 
     * @param request criterios de búsqueda
     * @return lista de canciones que coinciden con los criterios
     */
    public List<Cancion> buscar(SearchRequest request) {
        if (!request.tieneCriterios()) {
            return Collections.emptyList();
        }
        
        log.debug("Iniciando búsqueda avanzada con operador: {}", request.getOperador());
        
        // Búsquedas concurrentes por cada criterio
        CompletableFuture<List<Cancion>> futuroArtista = buscarPorArtista(request.getArtista());
        CompletableFuture<List<Cancion>> futuroGenero = buscarPorGenero(request.getGenero());
        CompletableFuture<List<Cancion>> futuroAno = buscarPorAno(request.getAño());
        
        // Esperar a que todas las búsquedas terminen
        CompletableFuture.allOf(futuroArtista, futuroGenero, futuroAno).join();
        
        try {
            List<Cancion> resultadosArtista = futuroArtista.get();
            List<Cancion> resultadosGenero = futuroGenero.get();
            List<Cancion> resultadosAno = futuroAno.get();
            
            // Combinar resultados según el operador lógico
            List<Cancion> resultado;
            if ("OR".equalsIgnoreCase(request.getOperador())) {
                resultado = combinarConOR(resultadosArtista, resultadosGenero, resultadosAno);
            } else {
                // Por defecto AND
                resultado = combinarConAND(resultadosArtista, resultadosGenero, resultadosAno);
            }
            
            log.debug("Búsqueda avanzada completada: {} resultados", resultado.size());
            return resultado;
            
        } catch (Exception e) {
            log.error("Error en búsqueda avanzada: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Busca canciones por artista de forma asíncrona.
     * 
     * @param artista nombre del artista
     * @return CompletableFuture con la lista de canciones
     */
    @Async
    public CompletableFuture<List<Cancion>> buscarPorArtista(String artista) {
        if (artista == null || artista.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        log.debug("Buscando por artista: {}", artista);
        List<Cancion> resultados = cancionRepository.findByArtista(artista);
        return CompletableFuture.completedFuture(resultados);
    }
    
    /**
     * Busca canciones por género de forma asíncrona.
     * 
     * @param genero género musical
     * @return CompletableFuture con la lista de canciones
     */
    @Async
    public CompletableFuture<List<Cancion>> buscarPorGenero(String genero) {
        if (genero == null || genero.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        log.debug("Buscando por género: {}", genero);
        List<Cancion> resultados = cancionRepository.findByGenero(genero);
        return CompletableFuture.completedFuture(resultados);
    }
    
    /**
     * Busca canciones por año de forma asíncrona.
     * 
     * @param año año de lanzamiento
     * @return CompletableFuture con la lista de canciones
     */
    @Async
    public CompletableFuture<List<Cancion>> buscarPorAno(Integer año) {
        if (año == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        log.debug("Buscando por año: {}", año);
        List<Cancion> resultados = cancionRepository.findByAño(año);
        return CompletableFuture.completedFuture(resultados);
    }
    
    /**
     * Combina resultados usando lógica AND (intersección).
     * Solo canciones que aparecen en todos los conjuntos de resultados.
     * 
     * @param resultados1 primer conjunto de resultados
     * @param resultados2 segundo conjunto de resultados
     * @param resultados3 tercer conjunto de resultados
     * @return lista de canciones que aparecen en todos los conjuntos
     */
    private List<Cancion> combinarConAND(List<Cancion> resultados1,
                                         List<Cancion> resultados2,
                                         List<Cancion> resultados3) {
        List<List<Cancion>> listasNoVacias = new ArrayList<>();
        
        if (!resultados1.isEmpty()) listasNoVacias.add(resultados1);
        if (!resultados2.isEmpty()) listasNoVacias.add(resultados2);
        if (!resultados3.isEmpty()) listasNoVacias.add(resultados3);
        
        if (listasNoVacias.isEmpty()) {
            return Collections.emptyList();
        }
        
        if (listasNoVacias.size() == 1) {
            return listasNoVacias.get(0);
        }
        
        // Empezar con el primer conjunto y hacer intersección con los demás
        Set<Cancion> interseccion = new HashSet<>(listasNoVacias.get(0));
        
        for (int i = 1; i < listasNoVacias.size(); i++) {
            interseccion.retainAll(listasNoVacias.get(i));
        }
        
        return new ArrayList<>(interseccion);
    }
    
    /**
     * Combina resultados usando lógica OR (unión).
     * Canciones que aparecen en al menos uno de los conjuntos de resultados.
     * 
     * @param resultados1 primer conjunto de resultados
     * @param resultados2 segundo conjunto de resultados
     * @param resultados3 tercer conjunto de resultados
     * @return lista de canciones que aparecen en al menos un conjunto (sin duplicados)
     */
    private List<Cancion> combinarConOR(List<Cancion> resultados1,
                                        List<Cancion> resultados2,
                                        List<Cancion> resultados3) {
        Set<Cancion> union = new LinkedHashSet<>();
        union.addAll(resultados1);
        union.addAll(resultados2);
        union.addAll(resultados3);
        
        return new ArrayList<>(union);
    }
}

