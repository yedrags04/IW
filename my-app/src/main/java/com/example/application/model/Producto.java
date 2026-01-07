package com.example.application.model; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/*
 * ENTIDAD PRODUCTO
 * Representa los platos o bebidas del catálogo de TuFood.
 * Incluye soporte para almacenamiento de imágenes binarias (BLOB).
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        // ID único del producto

    private String nombre;    // Nombre del plato o bebida
    private double precio;    // Precio unitario
    private String categoria; // Categoría (Ej: Hamburguesas, Bebidas, Postres)

    // Almacenamiento binario de la imagen del producto
    // @Lob y LONGBLOB permiten guardar archivos de imagen pesados directamente en la BD
    @Lob
    @Column(name = "imagen_blob", columnDefinition = "LONGBLOB")
    private byte[] imagenBlob;

    public Producto() {}

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public byte[] getImagenBlob() { return imagenBlob; }
    public void setImagenBlob(byte[] imagenBlob) { this.imagenBlob = imagenBlob; }

    // Métodos equals y hashCode basados en el ID para asegurar la unicidad en colecciones (Sets/Lists)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return id != null && id.equals(producto.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}