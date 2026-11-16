package com.cooperativa.coop_servicios_backend.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Solicitud de un cliente para actualizar sus datos. No toca la tabla clientes
 * hasta que un administrador la confirma.
 */
@Entity
@Table(name = "solicitudes_actualizacion_cliente")
public class SolicitudActualizacionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaResolucion;

    @Column(length = 100)
    private String resueltaPor;

    // ---- Datos propuestos ----
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 50)
    private String telefono;

    private String calle;
    private Integer numero;
    private String piso;
    private String departamento;
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "barrio_id")
    private Barrio barrio;

    public SolicitudActualizacionCliente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getResueltaPor() { return resueltaPor; }
    public void setResueltaPor(String resueltaPor) { this.resueltaPor = resueltaPor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Barrio getBarrio() { return barrio; }
    public void setBarrio(Barrio barrio) { this.barrio = barrio; }
}
