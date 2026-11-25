package com.cooperativa.coop_servicios_backend.DTOs;

import java.util.List;

public record FacturaResponse(
        Long id,
        String periodo,
        String fechaEmision,
        String fechaVencimiento,
        Double montoTotal,
        String estado,
        boolean vencida,
        List<String> servicios,
        String fechaPago,
        String idOperacion) {}
