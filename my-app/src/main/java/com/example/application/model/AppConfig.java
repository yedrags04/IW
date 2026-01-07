package com.example.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AppConfig {
    @Id
    private Long id = 1L; // Solo tendremos una fila de configuración

    private String nombreApp = "TuFood";
    private String colorPrimario = "#FF4D00"; // Naranja por defecto
    private String heroTitulo = "¡Tu comida favorita, a un clic!";
    private String heroSubtitulo = "Descubre los mejores sabores de tu ciudad.";

    // Getters y Setters
    public Long getId() { return id; }
    public String getNombreApp() { return nombreApp; }
    public void setNombreApp(String nombreApp) { this.nombreApp = nombreApp; }
    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }
    public String getHeroTitulo() { return heroTitulo; }
    public void setHeroTitulo(String heroTitulo) { this.heroTitulo = heroTitulo; }
    public String getHeroSubtitulo() { return heroSubtitulo; }
    public void setHeroSubtitulo(String heroSubtitulo) { this.heroSubtitulo = heroSubtitulo; }
}