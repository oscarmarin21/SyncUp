package com.syncup.util;

import com.syncup.model.Cancion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utilidad para exportar datos a formato CSV.
 * Requerido según RF-029.
 * 
 * @author SyncUp Team
 */
@Component
@Slf4j
public class CsvExporter {
    
    /**
     * Exporta una lista de canciones favoritas a formato CSV.
     * Requerido según RF-029.
     * 
     * @param favoritos lista de canciones favoritas
     * @return bytes del archivo CSV
     */
    public byte[] exportarFavoritos(List<Cancion> favoritos) {
        if (favoritos == null || favoritos.isEmpty()) {
            return "ID,Título,Artista,Género,Año,Duración\n".getBytes(StandardCharsets.UTF_8);
        }
        
        StringBuilder csv = new StringBuilder();
        
        // Encabezados
        csv.append("ID,Título,Artista,Género,Año,Duración (seg)\n");
        
        // Filas de datos
        for (Cancion cancion : favoritos) {
            csv.append(String.format("%d,%s,%s,%s,%d,%d\n",
                    cancion.getId(),
                    escaparCsv(cancion.getTitulo()),
                    escaparCsv(cancion.getArtista()),
                    escaparCsv(cancion.getGenero()),
                    cancion.getAño(),
                    cancion.getDuracion()
            ));
        }
        
        log.debug("CSV exportado con {} canciones", favoritos.size());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Escapa caracteres especiales para CSV (comillas y comas).
     * 
     * @param campo campo a escapar
     * @return campo escapado
     */
    private String escaparCsv(String campo) {
        if (campo == null) {
            return "";
        }
        
        // Si contiene comas, comillas o saltos de línea, envolver en comillas y escapar comillas
        if (campo.contains(",") || campo.contains("\"") || campo.contains("\n")) {
            return "\"" + campo.replace("\"", "\"\"") + "\"";
        }
        
        return campo;
    }
}

