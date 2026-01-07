package com.example.application.repository;

import com.example.application.model.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/*
 * REPOSITORIO PARA EL CATÁLOGO DE PRODUCTOS
 * Proporciona acceso a la carta del restaurante y filtrado por categorías.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // CONSULTA PERSONALIZADA (JPQL): 
    // Obtiene una lista única de nombres de categorías que tengan productos asociados.
    // Se utiliza para generar dinámicamente el menú de navegación por categorías.
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL")
    List<String> findDistinctCategorias();

    // Filtra la lista de productos por una categoría específica.
    // El parámetro 'Sort' permite ordenar los resultados (por precio, nombre, etc.).
    List<Producto> findByCategoria(String categoria, Sort sort);
}