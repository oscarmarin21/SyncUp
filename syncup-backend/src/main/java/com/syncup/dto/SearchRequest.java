package com.syncup.dto;

import lombok.Data;

/**
 * DTO para solicitudes de búsqueda avanzada de canciones.
 * Requerido según RF-004.
 * 
 * @author SyncUp Team
 */
@Data
public class SearchRequest {
    
    private String artista;
    private String genero;
    private Integer año;
    
    /**
     * Tipo de operador lógico para combinar criterios.
     * AND: todos los criterios deben coincidir
     * OR: al menos un criterio debe coincidir
     */
    private String operador = "AND"; // AND u OR
    
    /**
     * Verifica si la solicitud tiene criterios de búsqueda.
     * 
     * @return true si tiene al menos un criterio, false en caso contrario
     */
    public boolean tieneCriterios() {
        return (artista != null && !artista.trim().isEmpty()) ||
               (genero != null && !genero.trim().isEmpty()) ||
               año != null;
    }
}

