package com.example.application.model;

/*
 * MODELO MESA
 * Representa las mesas físicas del local. 
 * Esta clase se utiliza para controlar la disponibilidad de la sala y 
 * el balance económico actual de cada mesa abierta.
 */
public class Mesa {
    private Long id;              // Identificador único en la base de datos
    private int numeroMesa;       // Número identificativo (Mesa 1, Mesa 2, etc.)
    private String estado;        // Estado actual: "LIBRE" u "OCUPADA"
    private double totalAcumulado; // Suma total del precio de los productos consumidos

    public Mesa() {}

    public Mesa(Long id, int numeroMesa, String estado, double totalAcumulado) {
        this.id = id;
        this.numeroMesa = numeroMesa;
        this.estado = estado;
        this.totalAcumulado = totalAcumulado;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotalAcumulado() { return totalAcumulado; }
    public void setTotalAcumulado(double totalAcumulado) { this.totalAcumulado = totalAcumulado; }
}