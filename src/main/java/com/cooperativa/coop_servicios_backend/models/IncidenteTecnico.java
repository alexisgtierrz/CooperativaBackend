package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "incidentes_tecnicos")
public class IncidenteTecnico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 255)
    private String observaciones;
    @Column(nullable = false)
    private Boolean requiereVisita;

    // Relación Mucho a Uno (Varios incidentes pueden ser del mismo tipo)
    @ManyToOne
    @JoinColumn(name = "tipo_incidente_id", nullable = false)
    private TipoIncidenteTecnico tipoIncidente;

    public IncidenteTecnico() {}
    // Getters y Setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getRequiereVisita() {
        return requiereVisita;
    }

    public void setRequiereVisita(Boolean requiereVisita) {
        this.requiereVisita = requiereVisita;
    }

    public TipoIncidenteTecnico getTipoIncidente() {
        return tipoIncidente;
    }

    public void setTipoIncidente(TipoIncidenteTecnico tipoIncidente) {
        this.tipoIncidente = tipoIncidente;
    }
}
