package com.cooperativa.coop_servicios_backend.DTOs;

/**
 * Datos editables de un cliente. Sirve como cuerpo de la solicitud (barrioId)
 * y como forma de devolver datos actuales / propuestos (incluye nombres de barrio y localidad).
 */
public record DatosClienteDto(
        String nombre,
        String apellido,
        String telefono,
        String calle,
        Integer numero,
        String piso,
        String departamento,
        String observaciones,
        Long barrioId,
        String barrioNombre,
        Long localidadId,
        String localidadNombre
) {}
