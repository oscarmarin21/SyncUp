package com.syncup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configura manejadores de recursos estáticos adicionales para servir audios subidos.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path uploadDirectory;

    public StaticResourceConfig(@Value("${app.audio-upload-dir:uploads/audio}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDirectory.toUri().toString();
        registry.addResourceHandler("/audio/uploads/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/")
                .setCachePeriod(3600);
    }
}


