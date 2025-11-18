package com.syncup.service;

import com.syncup.model.Cancion;
import com.syncup.repository.CancionRepository;
import com.syncup.trie.TrieAutocompletado;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que gestiona el autocompletado de búsquedas usando Trie.
 * Requerido según RF-003, RF-025, RF-026.
 *
 * A partir de esta versión el autocompletado se realiza tanto por
 * título de la canción como por nombre del artista.
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
     * Se indexan tanto los títulos de las canciones como los nombres
     * de los artistas.
     */
    private final TrieAutocompletado trie = new TrieAutocompletado();

    /**
     * Inicializa el Trie cargando todas las canciones desde la base de datos.
     * Se ejecuta al arranque de la aplicación.
     * Complejidad: O(n*m) donde n es el número de canciones y m es la longitud promedio de los textos insertados.
     */
    @PostConstruct
    public void inicializar() {
        log.info("Inicializando Trie de autocompletado...");
        List<Cancion> canciones = cancionRepository.findAll();

        canciones.forEach(this::indexarCancionEnTrie);

        log.info("Trie inicializado con {} canciones", canciones.size());
    }

    /**
     * Indexa una canción en el Trie usando tanto el título como el nombre del artista
     * (si está disponible).
     *
     * @param cancion canción a indexar
     */
    private void indexarCancionEnTrie(Cancion cancion) {
        if (cancion.getTitulo() != null && !cancion.getTitulo().isBlank()) {
            trie.insertar(cancion.getTitulo(), cancion);
        }
        if (cancion.getArtista() != null && !cancion.getArtista().isBlank()) {
            trie.insertar(cancion.getArtista(), cancion);
        }
    }

    /**
     * Elimina del Trie todas las entradas asociadas a una canción
     * (título y artista, si existen).
     *
     * @param cancion canción a eliminar del índice
     */
    private void desindexarCancionEnTrie(Cancion cancion) {
        if (cancion.getTitulo() != null && !cancion.getTitulo().isBlank()) {
            trie.eliminar(cancion.getTitulo(), cancion);
        }
        if (cancion.getArtista() != null && !cancion.getArtista().isBlank()) {
            trie.eliminar(cancion.getArtista(), cancion);
        }
    }

    /**
     * Busca canciones cuyo título o artista comienzan con el prefijo dado.
     * Requerido según RF-003 y RF-026.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k es el número de resultados.
     *
     * Se realiza una deduplicación por identificador de canción, para evitar
     * que una misma canción aparezca dos veces cuando coincide tanto por título
     * como por artista.
     *
     * @param prefix prefijo de búsqueda (puede ser parte del título o del artista)
     * @return lista de canciones que coinciden con el prefijo
     */
    public List<Cancion> buscarPorPrefijo(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }

        List<Cancion> resultados = trie.buscarPorPrefijo(prefix);

        // Deduplicar por id manteniendo el orden de aparición
        Map<Long, Cancion> porId = new LinkedHashMap<>();
        for (Cancion c : resultados) {
            if (c.getId() != null) {
                porId.putIfAbsent(c.getId(), c);
            } else {
                // Si no tiene id (caso raro), igual la dejamos pasar usando hashCode como clave ficticia
                porId.putIfAbsent((long) c.hashCode(), c);
            }
        }

        return List.copyOf(porId.values());
    }

    /**
     * Agrega una nueva canción al Trie.
     * Útil cuando se crea una canción nueva.
     *
     * @param cancion canción a agregar
     */
    public void agregarCancion(Cancion cancion) {
        indexarCancionEnTrie(cancion);
        log.debug("Canción '{}' agregada al Trie", cancion.getTitulo());
    }

    /**
     * Elimina una canción del Trie.
     * Útil cuando se elimina una canción.
     *
     * @param cancion canción a eliminar
     */
    public void eliminarCancion(Cancion cancion) {
        desindexarCancionEnTrie(cancion);
        log.debug("Canción '{}' eliminada del Trie", cancion.getTitulo());
    }

    /**
     * Actualiza una canción en el Trie.
     * Primero elimina la versión antigua (si existe) y luego agrega la nueva.
     *
     * @param cancionAnterior canción anterior (si existe)
     * @param cancionNueva    canción nueva
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
        canciones.forEach(this::indexarCancionEnTrie);
        log.info("Trie reconstruido con {} canciones", canciones.size());
    }
}
