package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_incidente")
public class TipoIncidenteTecnico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, length = 50)
    private String gravedad;

    //Area/especialidad tecnica que resuelve este tipo de incidente.
    //Se compara con Empleado.especialidad
    @Column(length = 100)
    private String especialidad;
    public TipoIncidenteTecnico() {}

    // Getters y Setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getGravedad() {
        return gravedad;
    }

    public void setGravedad(String gravedad) {
        this.gravedad = gravedad;
    }
}
