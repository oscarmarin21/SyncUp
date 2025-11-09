package com.syncup.service;

import com.syncup.model.Cancion;
import com.syncup.repository.CancionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar canciones en el catálogo.
 * 
 * @author SyncUp Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CancionService {
    
    private final CancionRepository cancionRepository;
    private final com.syncup.service.AutocompletadoService autocompletadoService;
    private final AudioStorageService audioStorageService;

    private static final List<String> DEFAULT_AUDIO_TRACKS = List.of(
            "/audio/syncup_intro.wav",
            "/audio/syncup_groove.wav",
            "/audio/syncup_chill.wav"
    );
    
    /**
     * Obtiene todas las canciones del catálogo.
     * 
     * @return lista de todas las canciones
     */
    public List<Cancion> obtenerTodas() {
        return cancionRepository.findAll();
    }
    
    /**
     * Busca una canción por su ID.
     * 
     * @param id identificador de la canción
     * @return Optional con la canción encontrada
     */
    public Optional<Cancion> obtenerPorId(Long id) {
        return cancionRepository.findById(id);
    }
    
    /**
     * Busca canciones por autocompletado usando Trie.
     * Requerido según RF-003.
     * 
     * @param prefix prefijo de búsqueda
     * @return lista de canciones cuyo título comienza con el prefijo
     */
    public List<Cancion> buscarPorAutocompletado(String prefix) {
        return autocompletadoService.buscarPorPrefijo(prefix);
    }
    
    /**
     * Crea una nueva canción en el catálogo.
     * 
     * @param cancion canción a crear
     * @return canción creada
     */
    @Transactional
    public Cancion crear(Cancion cancion) {
        if (cancion.getAudioUrl() == null || cancion.getAudioUrl().isBlank() || !audioStorageService.exists(cancion.getAudioUrl())) {
            cancion.setAudioUrl(obtenerAudioPorDefecto(cancionRepository.count()));
        }

        Cancion cancionGuardada = cancionRepository.save(cancion);
        
        // Agregar al Trie de autocompletado
        autocompletadoService.agregarCancion(cancionGuardada);
        
        log.info("Canción '{}' creada con ID: {}", cancionGuardada.getTitulo(), cancionGuardada.getId());
        return cancionGuardada;
    }
    
    /**
     * Actualiza una canción existente.
     * 
     * @param id identificador de la canción
     * @param cancion datos actualizados
     * @return canción actualizada
     */
    @Transactional
    public Cancion actualizar(Long id, Cancion cancion) {
        Cancion cancionExistente = cancionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada"));
        
        Cancion cancionAnterior = new Cancion(
                cancionExistente.getId(),
                cancionExistente.getTitulo(),
                cancionExistente.getArtista(),
                cancionExistente.getGenero(),
                cancionExistente.getAño(),
                cancionExistente.getDuracion(),
                cancionExistente.getAudioUrl()
        );
        
        cancionExistente.setTitulo(cancion.getTitulo());
        cancionExistente.setArtista(cancion.getArtista());
        cancionExistente.setGenero(cancion.getGenero());
        cancionExistente.setAño(cancion.getAño());
        cancionExistente.setDuracion(cancion.getDuracion());
        if (cancion.getAudioUrl() != null && !cancion.getAudioUrl().isBlank() && audioStorageService.exists(cancion.getAudioUrl())) {
            cancionExistente.setAudioUrl(cancion.getAudioUrl());
        } else if (cancionExistente.getAudioUrl() == null || cancionExistente.getAudioUrl().isBlank() || !audioStorageService.exists(cancionExistente.getAudioUrl())) {
            cancionExistente.setAudioUrl(obtenerAudioPorDefecto(
                    cancionExistente.getId() != null ? cancionExistente.getId() : 0
            ));
        }
        
        Cancion cancionActualizada = cancionRepository.save(cancionExistente);
        
        // Actualizar en el Trie
        autocompletadoService.actualizarCancion(cancionAnterior, cancionActualizada);
        
        log.info("Canción con ID {} actualizada", id);
        return cancionActualizada;
    }
    
    /**
     * Elimina una canción del catálogo.
     * 
     * @param id identificador de la canción
     */
    @Transactional
    public void eliminar(Long id) {
        Cancion cancion = cancionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada"));
        
        cancionRepository.deleteById(id);
        
        // Eliminar del Trie
        autocompletadoService.eliminarCancion(cancion);
        
        log.info("Canción con ID {} eliminada", id);
    }

    private String obtenerAudioPorDefecto(long indice) {
        if (DEFAULT_AUDIO_TRACKS.isEmpty()) {
            return null;
        }
        for (int i = 0; i < DEFAULT_AUDIO_TRACKS.size(); i++) {
            int position = (int) ((Math.abs(indice) + i) % DEFAULT_AUDIO_TRACKS.size());
            String candidate = DEFAULT_AUDIO_TRACKS.get(position);
            if (audioStorageService.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}

