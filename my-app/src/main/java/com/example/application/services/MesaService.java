package com.example.application.services;

import com.example.application.model.Mesa;
import com.example.application.model.Producto;
import com.example.application.repository.ProductoRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SERVICIO DE GESTIÓN DE MESAS
 * Utiliza JdbcTemplate para operaciones de alta frecuencia en las mesas,
 * permitiendo un control preciso sobre la cuenta acumulada y el estado.
 */
@Service
public class MesaService {
    private final JdbcTemplate jdbc;

    public MesaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Recupera todas las mesas configuradas en el sistema ordenadas por su número.
     */
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
            return List.of(); // Devuelve lista vacía en caso de error para evitar NullPointerException
        }
    }

    /**
     * Consulta cuántas unidades de un producto específico hay registradas en una mesa.
     */
    public int obtenerCantidadProductoEnMesa(int numeroMesa, Long productoId) {
        try {
            Integer cantidad = jdbc.queryForObject(
                "SELECT cantidad FROM mesa_productos WHERE mesa_id = ? AND producto_id = ?", 
                Integer.class, numeroMesa, productoId);
            return cantidad != null ? cantidad : 0;
        } catch (Exception e) {
            return 0; // Si no existe el registro, la cantidad es cero
        }
    }

    /**
     * MODIFICACIÓN DE CANTIDADES (Lógica principal de comandas)
     * @param delta Cantidad a sumar o restar (ej: 1 o -1).
     * @param precio Precio unitario para actualizar el total acumulado de la mesa.
     */
    @Transactional // Asegura que si falla la actualización del total, no se guarde el producto
    public void modificarCantidad(int numeroMesa, Long productoId, int delta, double precio) {
        try {
            int nuevaCant = obtenerCantidadProductoEnMesa(numeroMesa, productoId) + delta;
            
            if (nuevaCant <= 0) {
                // Si la cantidad llega a 0 o menos, eliminamos el producto de la mesa
                jdbc.update("DELETE FROM mesa_productos WHERE mesa_id = ? AND producto_id = ?", numeroMesa, productoId);
            } else {
                // Si ya existe, actualiza; si no, inserta (Sintaxis MySQL/TiDB)
                jdbc.update("INSERT INTO mesa_productos (mesa_id, producto_id, cantidad) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE cantidad = ?", numeroMesa, productoId, nuevaCant, nuevaCant);
            }
            
            // Actualizamos el monedero/cuenta de la mesa sumando (delta * precio)
            jdbc.update("UPDATE mesas SET total_acumulado = total_acumulado + ? WHERE numero_mesa = ?", (delta * precio), numeroMesa);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el detalle completo de una cuenta (Platos y Cantidades).
     * Resuelve el problema de SQLException mediante un callback interno.
     */
    public Map<Producto, Integer> obtenerDetalleProductosMesa(int numeroMesa, ProductoRepository repo) {
        Map<Producto, Integer> detalle = new HashMap<>();
        try {
            jdbc.query("SELECT producto_id, cantidad FROM mesa_productos WHERE mesa_id = ?", (rs) -> {
                try {
                    while (rs.next()) {
                        Long pId = rs.getLong("producto_id");
                        int cant = rs.getInt("cantidad");
                        // Buscamos el objeto Producto completo en el repositorio JPA
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

    /**
     * Cambia el estado de la mesa (LIBRE, OCUPADA, PIDIENDO, PAGANDO).
     * Si se libera la mesa, se limpia automáticamente la cuenta y los productos.
     */
    @Transactional
    public void actualizarEstado(int numeroMesa, String nuevoEstado) {
        try {
            if ("LIBRE".equals(nuevoEstado)) {
                // Limpieza total para el siguiente cliente
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