package com.example.application.model;

public class Mesa {
    private Long id;
    private int numeroMesa;
    private String estado;
    private double totalAcumulado; // Campo necesario para la cuenta

    public Mesa() {}

    public Mesa(Long id, int numeroMesa, String estado, double totalAcumulado) {
        this.id = id;
        this.numeroMesa = numeroMesa;
        this.estado = estado;
        this.totalAcumulado = totalAcumulado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotalAcumulado() { return totalAcumulado; }
    public void setTotalAcumulado(double totalAcumulado) { this.totalAcumulado = totalAcumulado; }
}