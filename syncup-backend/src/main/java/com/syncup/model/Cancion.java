package com.syncup.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entidad que representa una pista musical en el catálogo de SyncUp.
 * Funciona como nodo en el Grafo de Similitud según RF-019.
 * 
 * @author SyncUp Team
 */
@Entity
@Table(name = "canciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cancion {
    
    /**
     * Identificador único de la canción.
     * Requerido según RF-018.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /**
     * Título de la canción.
     * Requerido según RF-018.
     */
    @NotBlank
    @Column(nullable = false)
    private String titulo;
    
    /**
     * Nombre del artista.
     * Requerido según RF-018.
     */
    @NotBlank
    @Column(nullable = false)
    private String artista;
    
    /**
     * Género musical de la canción.
     * Requerido según RF-018.
     */
    @NotBlank
    @Column(nullable = false)
    private String genero;
    
    /**
     * Año de lanzamiento de la canción.
     * Requerido según RF-018.
     */
    @NotNull
    @Min(1900)
    @Column(nullable = false)
    private Integer año;
    
    /**
     * Duración de la canción en segundos.
     * Requerido según RF-018.
     */
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer duracion;

    /**
     * URL del archivo de audio accesible para reproducción.
     * Opcional: cuando no se proporcione, el frontend puede manejarlo.
     */
    @Column(name = "audio_url")
    private String audioUrl;
    
    /**
     * Calcula el hash code basado en el id.
     * Requerido según RF-020.
     * 
     * @return hash code del id
     * Complejidad: O(1)
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Compara dos canciones basándose en el id.
     * Requerido según RF-020.
     * 
     * @param obj objeto a comparar
     * @return true si tienen el mismo id, false en caso contrario
     * Complejidad: O(1)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cancion cancion = (Cancion) obj;
        return Objects.equals(id, cancion.id);
    }
}

