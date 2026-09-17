package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "detalles_recorrido")
public class DetalleRecorrido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ordenVisita;

    @Column(nullable = false)
    private Boolean completado = false;

    public DetalleRecorrido() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getOrdenVisita() { return ordenVisita; }
    public void setOrdenVisita(Integer ordenVisita) { this.ordenVisita = ordenVisita; }
    public Boolean getCompletado() { return completado; }
    public void setCompletado(Boolean completado) { this.completado = completado; }
}
