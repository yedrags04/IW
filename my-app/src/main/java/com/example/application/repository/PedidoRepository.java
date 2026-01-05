package com.example.application.repository;

import com.example.application.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Spring Data JPA generará la consulta automáticamente
    long countByEstado(String estado);
}