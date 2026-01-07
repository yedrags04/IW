package com.example.application.services;

import com.example.application.model.Pedido;
import com.example.application.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class OrderService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Transactional
    public void actualizarEstado(Long id) {
        pedidoRepository.findById(id).ifPresent(pedido -> {
            String estadoActual = pedido.getEstado().toUpperCase();
            
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

public List<Pedido> getTodosLosPedidos() {
        return pedidoRepository.findAll(Sort.by(Sort.Direction.ASC, "fecha"));
    }

    public long contarPorEstado(String estado) {
        return pedidoRepository.countByEstado(estado);
    }
}