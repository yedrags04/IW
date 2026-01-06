package com.example.application.model;

public class Mesa {
    private Long id;
    private int numeroMesa;
    private String estado;

    public Mesa() {}

    public Mesa(Long id, int numeroMesa, String estado) {
        this.id = id;
        this.numeroMesa = numeroMesa;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}