package com.example.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/*
 * ENTIDAD PEDIDO
 * Representa una orden de comida enviada a cocina.
 * Almacena información tanto para el servicio en local (mesas) como para domicilio.
 */
@Entity
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_db;     // ID autoincremental para la persistencia en base de datos

    private String ticketId; // Código alfanumérico único para identificación del cliente
    private String cliente;  // Nombre del cliente o número de mesa
    private String tipo;     // Tipo de pedido: "LOCAL", "DOMICILIO" o "PARA LLEVAR"
    private String estado;   // Flujo de cocina: "NUEVO", "EN PREPARACIÓN", "LISTO", "FINALIZADO"
    private String direccion; // Dirección de entrega (solo para pedidos a domicilio)
    private LocalDateTime fecha; // Marca de tiempo de cuándo se creó el pedido

    public Pedido() {}

    public Pedido(String ticketId, String cliente, String tipo, String direccion) {
        this.ticketId = ticketId;
        this.cliente = cliente;
        this.tipo = tipo;
        this.direccion = direccion;
        this.estado = "NUEVO"; // Por defecto, todo pedido nace con estado NUEVO
        this.fecha = LocalDateTime.now();
    }

    // --- Getters y Setters ---
    public Long getId_db() { return id_db; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public LocalDateTime getFecha() { return fecha; }
    
    // Asigna la fecha manualmente (útil para registros históricos o correcciones)
    public void setFecha(LocalDateTime fecha) { 
        this.fecha = fecha; 
    }
}