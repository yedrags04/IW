package com.example.application.repository;

import com.example.application.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Aquí puedes añadir métodos personalizados si los necesitas, ej:
    // List<Pedido> findByEstado(String estado);
}