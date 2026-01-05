package com.example.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_db;

    private String ticketId; 
    private String cliente;
    private String tipo; 
    private String estado; 
    private String direccion;
    private LocalDateTime fecha;

    public Pedido() {}

    public Pedido(String ticketId, String cliente, String tipo, String direccion) {
        this.ticketId = ticketId;
        this.cliente = cliente;
        this.tipo = tipo;
        this.direccion = direccion;
        this.estado = "NUEVO"; // Estado inicial unificado
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters corregidos
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
    
    // Método necesario para registrar la fecha en el Service
    public void setFecha(LocalDateTime fecha) { 
        this.fecha = fecha; 
    }
}