package com.example.application.services;

import com.example.application.model.Mesa;
import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MesaService {
    private final JdbcTemplate jdbc;

    public MesaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Mesa> obtenerTodas() {
        try {
            return jdbc.query("SELECT * FROM mesas ORDER BY numero_mesa ASC", (rs, rowNum) -> {
                Mesa m = new Mesa();
                m.setId(rs.getLong("id"));
                m.setNumeroMesa(rs.getInt("numero_mesa"));
                m.setEstado(rs.getString("estado"));
                m.setTotalAcumulado(rs.getDouble("total_acumulado"));
                return m;
            });
        } catch (DataAccessException e) {
            return List.of();
        }
    }

    public int obtenerCantidadProductoEnMesa(int numeroMesa, Long productoId) {
        try {
            Integer cantidad = jdbc.queryForObject(
                "SELECT cantidad FROM mesa_productos WHERE mesa_id = ? AND producto_id = ?", 
                Integer.class, numeroMesa, productoId);
            return cantidad != null ? cantidad : 0;
        } catch (Exception e) {
            return 0; 
        }
    }

    public void modificarCantidad(int numeroMesa, Long productoId, int delta, double precio) {
        try {
            int nuevaCant = obtenerCantidadProductoEnMesa(numeroMesa, productoId) + delta;
            if (nuevaCant <= 0) {
                jdbc.update("DELETE FROM mesa_productos WHERE mesa_id = ? AND producto_id = ?", numeroMesa, productoId);
            } else {
                jdbc.update("INSERT INTO mesa_productos (mesa_id, producto_id, cantidad) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE cantidad = ?", numeroMesa, productoId, nuevaCant, nuevaCant);
            }
            jdbc.update("UPDATE mesas SET total_acumulado = total_acumulado + ? WHERE numero_mesa = ?", (delta * precio), numeroMesa);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    // --- AQUÍ ESTÁ LA CORRECCIÓN ESPECÍFICA ---
    public Map<Producto, Integer> obtenerDetalleProductosMesa(int numeroMesa, ProductoRepository repo) {
        Map<Producto, Integer> detalle = new HashMap<>();
        try {
            // Usamos una versión de query que maneja internamente el ResultSet
            jdbc.query("SELECT producto_id, cantidad FROM mesa_productos WHERE mesa_id = ?", (rs) -> {
                // El bloque try-catch interno soluciona el "Unhandled exception type SQLException"
                try {
                    while (rs.next()) {
                        Long pId = rs.getLong("producto_id");
                        int cant = rs.getInt("cantidad");
                        repo.findById(pId).ifPresent(p -> detalle.put(p, cant));
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando ResultSet: " + e.getMessage());
                }
                return detalle;
            }, numeroMesa);
        } catch (DataAccessException e) {
            System.err.println("Error de acceso a datos: " + e.getMessage());
        }
        return detalle;
    }

    public void actualizarEstado(int numeroMesa, String nuevoEstado) {
        try {
            if ("LIBRE".equals(nuevoEstado)) {
                jdbc.update("UPDATE mesas SET estado = ?, total_acumulado = 0.0 WHERE numero_mesa = ?", nuevoEstado, numeroMesa);
                jdbc.update("DELETE FROM mesa_productos WHERE mesa_id = ?", numeroMesa);
            } else {
                jdbc.update("UPDATE mesas SET estado = ? WHERE numero_mesa = ?", nuevoEstado, numeroMesa);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}