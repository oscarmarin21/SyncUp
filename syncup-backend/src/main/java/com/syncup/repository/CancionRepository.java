package com.syncup.repository;

import com.syncup.model.Cancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Cancion.
 * Proporciona acceso a los datos de canciones almacenadas en la base de datos.
 * 
 * @author SyncUp Team
 */
@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {
    
    /**
     * Busca canciones por artista.
     * 
     * @param artista nombre del artista
     * @return lista de canciones del artista
     * Complejidad: O(n) donde n es el número de canciones
     */
    List<Cancion> findByArtista(String artista);
    
    /**
     * Busca canciones por género.
     * 
     * @param genero género musical
     * @return lista de canciones del género
     * Complejidad: O(n) donde n es el número de canciones
     */
    List<Cancion> findByGenero(String genero);
    
    /**
     * Busca canciones por año.
     * 
     * @param año año de lanzamiento
     * @return lista de canciones del año
     * Complejidad: O(n) donde n es el número de canciones
     */
    List<Cancion> findByAño(Integer año);
    
    /**
     * Busca canciones por artista y género.
     * 
     * @param artista nombre del artista
     * @param genero género musical
     * @return lista de canciones que coinciden con ambos criterios
     * Complejidad: O(n) donde n es el número de canciones
     */
    List<Cancion> findByArtistaAndGenero(String artista, String genero);
    
    /**
     * Busca canciones cuyo título contiene el texto dado (case-insensitive).
     * 
     * @param titulo texto a buscar en el título
     * @return lista de canciones cuyo título contiene el texto
     * Complejidad: O(n) donde n es el número de canciones
     */
    @Query("SELECT c FROM Cancion c WHERE LOWER(c.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Cancion> findByTituloContainingIgnoreCase(@Param("titulo") String titulo);
    
    /**
     * Busca una canción por título y artista exactos.
     * 
     * @param titulo título de la canción
     * @param artista nombre del artista
     * @return canción opcional si existe
     */
    @Query("SELECT c FROM Cancion c WHERE c.titulo = :titulo AND c.artista = :artista")
    java.util.Optional<Cancion> findByTituloAndArtista(@Param("titulo") String titulo, @Param("artista") String artista);
}

