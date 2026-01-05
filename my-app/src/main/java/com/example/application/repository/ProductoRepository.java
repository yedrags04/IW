package com.example.application.repository;

import com.example.application.model.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Obtiene solo las categorías que tienen al menos un producto asignado
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL")
    List<String> findDistinctCategorias();

    // Busca productos por categoría con ordenación
    List<Producto> findByCategoria(String categoria, Sort sort);
}