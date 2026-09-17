package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "planes_visita")
public class PlanDeVisita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaPlanificada;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "plan_visita_id", nullable = false)
    private List<Recorrido> recorridos;

    public PlanDeVisita() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFechaPlanificada() { return fechaPlanificada; }
    public void setFechaPlanificada(LocalDate fechaPlanificada) { this.fechaPlanificada = fechaPlanificada; }
    public List<Recorrido> getRecorridos() { return recorridos; }
    public void setRecorridos(List<Recorrido> recorridos) { this.recorridos = recorridos; }
}
