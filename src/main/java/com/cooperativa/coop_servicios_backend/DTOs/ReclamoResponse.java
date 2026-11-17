package com.cooperativa.coop_servicios_backend.DTOs;

import java.time.LocalDate;
import java.util.List;

//Vista de un reclamo. Estado: PENDIENTE | EN_PROCESO | RESUELTO
public record ReclamoResponse(
        Long id,
        LocalDate fechaCreacion,
        String descripcion,
        Long tipoIncidenteId,
        String tipoIncidenteNombre,
        String gravedad,
        String especialidad,
        String estado,
        String estadoNombre,
        String tecnicoNombre,
        String observacionesResolucion,
        List<Seguimiento> historial,
        ClienteResumen cliente
) {
    public record Seguimiento(String estado, LocalDate desde, LocalDate hasta, String tecnico) {}

    public record ClienteResumen(Long id, String nombre, String apellido, String telefono, String domicilio) {}
}
