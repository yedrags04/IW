package com.example.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/* * CLASE DE CONFIGURACIÓN GLOBAL DE LA APLICACIÓN
 * Esta entidad permite persistir en la base de datos los aspectos visuales 
 * y textos principales de la web, permitiendo que sean personalizables 
 * desde un panel de administración sin tocar el código fuente.
 */
@Entity
public class AppConfig {

    // Identificador único fijo. 
    // Forzamos el ID a 1L porque solo queremos una única fila de configuración en la tabla.
    @Id
    private Long id = 1L; 

    // Nombre comercial de la aplicación (Ej: TuFood)
    private String nombreApp = "TuFood";

    // Código hexadecimal del color corporativo utilizado en botones y elementos destacados
    private String colorPrimario = "#FF4D00"; 

    // Texto principal que aparece en la sección central (Hero) de la página de inicio
    private String heroTitulo = "¡Tu comida favorita, a un clic!";

    // Texto secundario o descripción que acompaña al título principal
    private String heroSubtitulo = "Descubre los mejores sabores de tu ciudad.";

    // Constructor vacío requerido por JPA para instanciar la entidad desde la BD
    public AppConfig() {
    }

    // --- MÉTODOS GETTERS Y SETTERS ---
    // Permiten la lectura y escritura de los parámetros de configuración por el resto de la app.

    public Long getId() { 
        return id; 
    }

    public String getNombreApp() { 
        return nombreApp; 
    }

    public void setNombreApp(String nombreApp) { 
        this.nombreApp = nombreApp; 
    }

    public String getColorPrimario() { 
        return colorPrimario; 
    }

    public void setColorPrimario(String colorPrimario) { 
        this.colorPrimario = colorPrimario; 
    }

    public String getHeroTitulo() { 
        return heroTitulo; 
    }

    public void setHeroTitulo(String heroTitulo) { 
        this.heroTitulo = heroTitulo; 
    }

    public String getHeroSubtitulo() { 
        return heroSubtitulo; 
    }

    public void setHeroSubtitulo(String heroSubtitulo) { 
        this.heroSubtitulo = heroSubtitulo; 
    }
}