package com.example.application.repository;

import com.example.application.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * REPOSITORIO PARA PEDIDOS
 * Encargado de gestionar las órdenes enviadas a cocina y el histórico de ventas.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    // Método por convención de nombre: Cuenta cuántos pedidos hay en un estado concreto.
    // Útil para mostrar contadores de "Pedidos Pendientes" en el dashboard de cocina.
    long countByEstado(String estado);
}