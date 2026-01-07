package com.example.application.services;

import com.example.application.model.Pedido;
import com.example.application.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 * SERVICIO DE GESTIÓN DE PEDIDOS
 * Controla el ciclo de vida de una orden desde que entra en cocina hasta que se entrega.
 */
@Service
public class OrderService {

    @Autowired
    private PedidoRepository pedidoRepository;

    /**
     * AVANCE DE ESTADO (Workflow de Cocina)
     * Cambia el estado del pedido siguiendo el flujo lógico del restaurante.
     */
    @Transactional
    public void actualizarEstado(Long id) {
        pedidoRepository.findById(id).ifPresent(pedido -> {
            String estadoActual = pedido.getEstado().toUpperCase();
            
            // Lógica de "Siguiente Paso"
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
            // Al estar en un método @Transactional, el guardado es automático al terminar
            pedidoRepository.save(pedido);
        });
    }

    /**
     * Registra un nuevo pedido en el sistema.
     * Asigna automáticamente la fecha actual y el estado inicial si no existen.
     */
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

    /**
     * Recupera todos los pedidos del sistema ordenados por antigüedad.
     * Ideal para que cocina atienda primero a quien lleva más tiempo esperando.
     */
    public List<Pedido> getTodosLosPedidos() {
        return pedidoRepository.findAll(Sort.by(Sort.Direction.ASC, "fecha"));
    }

    /**
     * Cuenta cuántos pedidos hay en un estado específico (Útil para KPIs y estadísticas).
     */
    public long contarPorEstado(String estado) {
        return pedidoRepository.countByEstado(estado);
    }
}