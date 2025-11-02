package com.example.vaadinweb.repository;

import com.example.vaadinweb.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
