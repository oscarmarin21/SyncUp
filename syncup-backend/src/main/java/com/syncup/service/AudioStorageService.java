package com.syncup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio encargado de almacenar archivos de audio subidos por los administradores.
 * Los archivos se guardan en el sistema de archivos y se exponen bajo la ruta /audio/uploads/**.
 *
 * Requerido para habilitar carga de canciones personalizadas.
 *
 * @author SyncUp
 */
@Service
@Slf4j
public class AudioStorageService {

    private static final Set<String> AUDIO_CONTENT_TYPES = Set.of(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/x-wav", "audio/flac", "audio/ogg", "audio/aac"
    );

    private final Path uploadDirectory;

    public AudioStorageService(@Value("${app.audio-upload-dir:uploads/audio}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
            log.info("Directorio de audio configurado en {}", this.uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio para audios: " + uploadDir, e);
        }
    }

    /**
     * Almacena un archivo de audio en el sistema de archivos y devuelve la URL pública.
     *
     * @param file archivo recibido del frontend
     * @return URL pública para acceder al audio
     * @throws IllegalArgumentException si el archivo no es válido
     */
    public String storeAudio(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de audio está vacío");
        }

        if (!esAudioValido(file)) {
            throw new IllegalArgumentException("Tipo de archivo no soportado. Solo se permiten archivos de audio.");
        }

        String extension = obtenerExtension(file.getOriginalFilename());
        if (extension.isBlank()) {
            extension = ".mp3";
        }

        String fileName = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(fileName);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        log.info("Audio almacenado en {}", destination);

        return "/audio/uploads/" + fileName;
    }

    /**
     * Verifica si existe un recurso de audio, ya sea en el sistema de archivos o en el classpath.
     *
     * @param audioUrl ruta pública del audio
     * @return true si el archivo está disponible
     */
    public boolean exists(String audioUrl) {
        if (audioUrl == null || audioUrl.isBlank()) {
            return false;
        }

        if (audioUrl.startsWith("/audio/uploads/")) {
            String relativePath = audioUrl.substring("/audio/uploads/".length());
            return Files.exists(uploadDirectory.resolve(relativePath));
        }

        String classpathLocation = audioUrl.startsWith("/") ? "static" + audioUrl : audioUrl;
        return AudioStorageService.class.getClassLoader().getResource(classpathLocation) != null;
    }

    private boolean esAudioValido(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && AUDIO_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }

        String extension = obtenerExtension(file.getOriginalFilename()).toLowerCase();
        return extension.equals(".mp3") || extension.equals(".wav") || extension.equals(".flac")
                || extension.equals(".ogg") || extension.equals(".aac") || extension.equals(".m4a");
    }

    private String obtenerExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }

        String cleaned = StringUtils.cleanPath(originalFilename);
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return cleaned.substring(dotIndex).toLowerCase();
    }
}


