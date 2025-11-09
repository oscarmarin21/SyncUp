package com.syncup.service;

import com.syncup.model.Cancion;
import com.syncup.repository.CancionRepository;
import com.syncup.trie.TrieAutocompletado;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que gestiona el autocompletado de búsquedas usando Trie.
 * Requerido según RF-003, RF-025, RF-026.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompletadoService {
    
    private final CancionRepository cancionRepository;
    
    /**
     * Instancia del Trie para búsquedas eficientes por prefijo.
     */
    private final TrieAutocompletado trie = new TrieAutocompletado();
    
    /**
     * Inicializa el Trie cargando todas las canciones desde la base de datos.
     * Se ejecuta al arranque de la aplicación.
     * Complejidad: O(n*m) donde n es el número de canciones y m es la longitud promedio de los títulos
     */
    @PostConstruct
    public void inicializar() {
        log.info("Inicializando Trie de autocompletado...");
        List<Cancion> canciones = cancionRepository.findAll();
        
        canciones.forEach(cancion -> {
            trie.insertar(cancion.getTitulo(), cancion);
        });
        
        log.info("Trie inicializado con {} canciones", canciones.size());
    }
    
    /**
     * Busca canciones cuyo título comienza con el prefijo dado.
     * Requerido según RF-003 y RF-026.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k es el número de resultados
     * 
     * @param prefix prefijo de búsqueda
     * @return lista de canciones que coinciden con el prefijo
     */
    public List<Cancion> buscarPorPrefijo(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }
        
        return trie.buscarPorPrefijo(prefix);
    }
    
    /**
     * Agrega una nueva canción al Trie.
     * Útil cuando se crea una canción nueva.
     * 
     * @param cancion canción a agregar
     */
    public void agregarCancion(Cancion cancion) {
        trie.insertar(cancion.getTitulo(), cancion);
        log.debug("Canción '{}' agregada al Trie", cancion.getTitulo());
    }
    
    /**
     * Elimina una canción del Trie.
     * Útil cuando se elimina una canción.
     * 
     * @param cancion canción a eliminar
     */
    public void eliminarCancion(Cancion cancion) {
        trie.eliminar(cancion.getTitulo(), cancion);
        log.debug("Canción '{}' eliminada del Trie", cancion.getTitulo());
    }
    
    /**
     * Actualiza una canción en el Trie.
     * Primero elimina la versión antigua y luego agrega la nueva.
     * 
     * @param cancionAnterior canción anterior (si existe)
     * @param cancionNueva canción nueva
     */
    public void actualizarCancion(Cancion cancionAnterior, Cancion cancionNueva) {
        if (cancionAnterior != null) {
            eliminarCancion(cancionAnterior);
        }
        agregarCancion(cancionNueva);
    }

    /**
     * Reconstruye el Trie desde la base de datos, útil cuando se realizan
     * operaciones masivas (por ejemplo, inicialización de datos).
     */
    public synchronized void reconstruirDesdeBD() {
        log.info("Reconstruyendo Trie de autocompletado desde la base de datos...");
        trie.limpiar();
        List<Cancion> canciones = cancionRepository.findAll();
        canciones.forEach(cancion -> trie.insertar(cancion.getTitulo(), cancion));
        log.info("Trie reconstruido con {} canciones", canciones.size());
    }
}

