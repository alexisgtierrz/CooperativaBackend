package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(length = 100)
    private String categoria;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "incidente_tecnico_id", referencedColumnName = "id")
    private IncidenteTecnico incidenteTecnico;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ticket_id")
    private java.util.List<CambioEstado> historialEstados;

    public Ticket() {}

    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public IncidenteTecnico getIncidenteTecnico() { return incidenteTecnico; }
    public void setIncidenteTecnico(IncidenteTecnico incidenteTecnico) { this.incidenteTecnico = incidenteTecnico; }
    public java.util.List<CambioEstado> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(java.util.List<CambioEstado> historialEstados) { this.historialEstados = historialEstados; }
}