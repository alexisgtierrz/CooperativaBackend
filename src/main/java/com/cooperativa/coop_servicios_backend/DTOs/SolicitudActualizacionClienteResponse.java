package com.cooperativa.coop_servicios_backend.DTOs;

import java.time.LocalDateTime;

public record SolicitudActualizacionClienteResponse(
        Long id,
        String estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaResolucion,
        String resueltaPor,
        Long clienteId,
        String clienteDni,
        DatosClienteDto actual,
        DatosClienteDto propuesto
) {}
