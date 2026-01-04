package com.example.application.services;

import com.example.application.model.Pedido;
import com.example.application.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {
    private final PedidoRepository repository;

    public OrderService(PedidoRepository repository) {
        this.repository = repository;
    }

    public void registrarPedido(Pedido pedido) {
        repository.save(pedido); // Guarda en la BD
    }

    public List<Pedido> getTodosLosPedidos() {
        return repository.findAll(); // Lee de la BD
    }

    public void actualizarEstado(Long id_db) {
        repository.findById(id_db).ifPresent(p -> {
            if (p.getEstado().equals("NUEVO")) p.setEstado("COCINANDO");
            else if (p.getEstado().equals("COCINANDO")) p.setEstado("LISTO");
            repository.save(p); // Actualiza en la BD
        });
    }

    public long contarPorEstado(String estado) {
        return getTodosLosPedidos().stream()
                .filter(p -> p.getEstado().equals(estado))
                .count();
    }
}