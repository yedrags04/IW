package com.example.application.services;

import com.example.application.model.Pedido;
import com.example.application.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.Sort;

/* * SERVICIO DE GESTIÓN DE PEDIDOS (COCINA)
 * Controla el ciclo de vida de un pedido desde que entra como "NUEVO" 
 * hasta que se entrega al cliente.
 */
@Service
public class OrderService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Lógica de máquina de estados para avanzar el proceso de cocina
    @Transactional
    public void actualizarEstado(Long id) {
        pedidoRepository.findById(id).ifPresent(pedido -> {
            String estadoActual = pedido.getEstado().toUpperCase();
            
            // Flujo secuencial: NUEVO -> EN PREPARACIÓN -> LISTO -> ENTREGADO
            switch (estadoActual) {
                case "NUEVO":
                    pedido.setEstado("EN PREPARACION");
                    break;
                case "EN PREPARACION":
                    pedido.setEstado("LISTO");
                    break;
                case "LISTO":
                    pedido.setEstado("ENTREGADO");
                    break;
            }
            pedidoRepository.save(pedido);
        });
    }

    // Registra un nuevo pedido asegurando valores por defecto si faltan
    @Transactional
    public Pedido registrarPedido(Pedido nuevoPedido) {
        if (nuevoPedido.getEstado() == null) {
            nuevoPedido.setEstado("NUEVO");
        }
        if (nuevoPedido.getFecha() == null) {
            nuevoPedido.setFecha(java.time.LocalDateTime.now());
        }
        return pedidoRepository.save(nuevoPedido);
    }

    // Recupera todo el histórico de pedidos ordenados por fecha de llegada
    public List<Pedido> getTodosLosPedidos() {
        return pedidoRepository.findAll(Sort.by(Sort.Direction.ASC, "fecha"));
    }

    // Cuenta cuántos pedidos hay en un estado específico (Útil para notificaciones)
    public long contarPorEstado(String estado) {
        return pedidoRepository.countByEstado(estado);
    }
}