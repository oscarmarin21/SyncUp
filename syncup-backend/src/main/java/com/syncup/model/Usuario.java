package com.syncup.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entidad que representa a los usuarios de la plataforma SyncUp.
 * Almacena información del usuario incluyendo credenciales y perfil.
 * 
 * @author SyncUp Team
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /**
     * Nombre de usuario único para login.
     * Requerido según RF-015.
     */
    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;
    
    /**
     * Contraseña hasheada del usuario.
     * Requerido según RF-015.
     */
    @NotBlank
    @Column(nullable = false)
    private String password;
    
    /**
     * Nombre completo del usuario.
     * Requerido según RF-015.
     */
    @NotBlank
    @Column(nullable = false)
    private String nombre;
    
    /**
     * Rol del usuario (USER o ADMIN).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.USER;
    
    /**
     * Enum que representa los roles disponibles en el sistema.
     */
    public enum Rol {
        USER, ADMIN
    }
    
    /**
     * Calcula el hash code basado en el username.
     * Requerido según RF-017.
     * 
     * @return hash code del username
     * Complejidad: O(1)
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
    
    /**
     * Compara dos usuarios basándose en el username.
     * Requerido según RF-017.
     * 
     * @param obj objeto a comparar
     * @return true si tienen el mismo username, false en caso contrario
     * Complejidad: O(1)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return Objects.equals(username, usuario.username);
    }
}

