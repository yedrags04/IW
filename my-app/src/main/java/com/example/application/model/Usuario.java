package com.example.application.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/*
 * ENTIDAD USUARIO
 * Representa a los usuarios del sistema (Clientes, Trabajadores, Admins).
 * Implementa validaciones de Bean Validation para garantizar la integridad de los datos.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de usuario (Login)
    @Column(nullable = false, unique = true) // No nulo y único en la tabla
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
    @NotEmpty(message = "El nombre de usuario no puede estar vacío.")
    private String nombre; 

    // Contraseña (debe almacenarse encriptada por el servicio)
    @Column(nullable = false)
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    @NotEmpty(message = "La contraseña no puede estar vacía.")
    private String contrasena;
    
    // Rol del usuario: "CLIENTE", "TRABAJADOR", "ADMIN"
    @Column(nullable = false)
    private String rol; 

    // Correo electrónico de contacto
    @Column(nullable = false, unique = true)
    @Email(message = "Debe ser un formato de email válido.")
    @NotEmpty(message = "El email no puede estar vacío.")
    private String email; 

    public Usuario() {}

    public Usuario(String nombre, String contrasena, String rol, String email) {
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.rol = rol;
        this.email = email;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}