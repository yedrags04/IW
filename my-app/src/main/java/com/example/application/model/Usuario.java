package com.example.application.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String contrasena;
    
    // 🔑 Campo nuevo para almacenar el rol del usuario
    @Column(nullable = false)
    private String rol; 

    public Usuario() {}

    public Usuario(String nombre, String contrasena, String rol) {
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    // 👈 Getters y Setters para el nuevo campo 'rol'
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}