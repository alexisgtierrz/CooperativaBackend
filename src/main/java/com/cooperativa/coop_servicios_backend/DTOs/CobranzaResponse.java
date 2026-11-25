package com.cooperativa.coop_servicios_backend.DTOs;

import java.util.List;

//Situacion de cobranza de un cliente: servicios, facturas y deuda (vista de administrador)
public record CobranzaResponse(
        Long clienteId,
        String nombre,
        String dni,
        String email,
        String telefono,
        boolean activo,
        List<ServicioContratado> servicios,
        int facturasPendientes,
        int facturasVencidas,
        double deudaTotal,
        double deudaVencida,
        long diasMaxAtraso,
        String proximoVencimiento,
        List<FacturaResponse> facturas) {

    public record ServicioContratado(String nombre, String vigenteHasta) {}
}
