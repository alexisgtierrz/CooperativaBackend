package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "permisos")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Relación Muchos a Uno: Varios permisos pertenecen a un perfil
    @ManyToOne
    @JoinColumn(name = "perfil_id", nullable = false)
    @JsonIgnore // Evita el bucle infinito al traer el perfil
    private Perfil perfil;

    public Permiso() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
}