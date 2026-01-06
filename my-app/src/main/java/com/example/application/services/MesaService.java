package com.example.application.services;

import com.example.application.model.Mesa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MesaService {

    private final JdbcTemplate jdbc;

    public MesaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Mesa> obtenerTodas() {
        String sql = "SELECT * FROM mesas ORDER BY numero_mesa ASC";
        return jdbc.query(sql, (rs, rowNum) -> new Mesa(
                rs.getLong("id"),
                rs.getInt("numero_mesa"),
                rs.getString("estado")
        ));
    }

public void añadirMesa() {
    // Buscamos el número de mesa más alto que ya existe
    Integer maxMesa = jdbc.queryForObject("SELECT COALESCE(MAX(numero_mesa), 0) FROM mesas", Integer.class);
    
    // Insertamos el siguiente (max + 1), así nunca habrá duplicados
    jdbc.update("INSERT INTO mesas (numero_mesa, estado, total_acumulado) VALUES (?, 'LIBRE', 0.00)", maxMesa + 1);
}

    public void eliminarUltimaMesa() {
        jdbc.update("DELETE FROM mesas WHERE numero_mesa = (SELECT MAX(numero_mesa) FROM mesas)");
    }

    public void actualizarEstado(int numeroMesa, String nuevoEstado) {
        jdbc.update("UPDATE mesas SET estado = ? WHERE numero_mesa = ?", nuevoEstado, numeroMesa);
    }
}