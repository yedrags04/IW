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

/* * SERVICIO DE GESTIÓN DE MESAS
 * Utiliza JdbcTemplate para realizar operaciones directas sobre la base de datos.
 * Controla la relación entre mesas y productos (comandas).
 */
@Service
public class MesaService {
    private final JdbcTemplate jdbc; // Herramienta de Spring para ejecutar SQL puro

    public MesaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Recupera la lista de todas las mesas ordenadas por su número
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
            return List.of(); // Devuelve lista vacía en caso de error de conexión
        }
    }

    // Consulta cuántas unidades de un producto específico hay en una mesa
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

    // Incrementa o decrementa la cantidad de un plato en la comanda de una mesa
    public void modificarCantidad(int numeroMesa, Long productoId, int delta, double precio) {
        try {
            int nuevaCant = obtenerCantidadProductoEnMesa(numeroMesa, productoId) + delta;
            
            // Si la cantidad llega a 0 o menos, eliminamos el registro de la comanda
            if (nuevaCant <= 0) {
                jdbc.update("DELETE FROM mesa_productos WHERE mesa_id = ? AND producto_id = ?", numeroMesa, productoId);
            } else {
                // Si existe lo actualiza, si no lo inserta (Operación Atómica)
                jdbc.update("INSERT INTO mesa_productos (mesa_id, producto_id, cantidad) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE cantidad = ?", numeroMesa, productoId, nuevaCant, nuevaCant);
            }
            // Actualizamos el balance económico de la mesa (total acumulado)
            jdbc.update("UPDATE mesas SET total_acumulado = total_acumulado + ? WHERE numero_mesa = ?", (delta * precio), numeroMesa);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    // Obtiene un resumen detallado de qué productos y qué cantidad tiene una mesa actualmente
    public Map<Producto, Integer> obtenerDetalleProductosMesa(int numeroMesa, ProductoRepository repo) {
        Map<Producto, Integer> detalle = new HashMap<>();
        try {
            jdbc.query("SELECT producto_id, cantidad FROM mesa_productos WHERE mesa_id = ?", (rs) -> {
                try {
                    while (rs.next()) {
                        Long pId = rs.getLong("producto_id");
                        int cant = rs.getInt("cantidad");
                        // Buscamos el objeto Producto completo en el repositorio
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

    // Cambia el estado (Libre/Ocupada) y limpia la comanda si se libera la mesa
    public void actualizarEstado(int numeroMesa, String nuevoEstado) {
        try {
            if ("LIBRE".equals(nuevoEstado)) {
                // Reset de mesa: balance a 0 y borrado de productos asociados
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