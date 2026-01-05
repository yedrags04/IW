package com.example.application.services;

import com.example.application.model.Pedido;
import com.example.application.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired; // Necesario
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Necesario
import java.util.List;

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

    public List<Pedido> getTodosLosPedidos() {
        // Podrías devolverlos ordenados por fecha para una "Toma de Pedidos Ágil" [cite: 48]
        return pedidoRepository.findAll();
    }

    public long contarPorEstado(String estado) {
        return pedidoRepository.countByEstado(estado);
    }
}