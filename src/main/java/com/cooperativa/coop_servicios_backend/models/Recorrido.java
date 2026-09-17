package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "recorridos")
public class Recorrido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaEjecucion;

    @Column(nullable = false)
    private Double distancia;

    @ManyToOne
    @JoinColumn(name = "domicilio_id", nullable = false)
    private Domicilio domicilio;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recorrido_id", nullable = false)
    private List<DetalleRecorrido> detalles;

    public Recorrido() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(LocalDate fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }
    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }
    public Domicilio getDomicilio() { return domicilio; }
    public void setDomicilio(Domicilio domicilio) { this.domicilio = domicilio; }
    public List<DetalleRecorrido> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleRecorrido> detalles) { this.detalles = detalles; }
}
