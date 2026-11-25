package com.cooperativa.coop_servicios_backend.DTOs;

public final class PagoRequests {
    private PagoRequests() {}

    public record CrearPreferencia(Long facturaId) {}

    public record Confirmar(String paymentId) {}

    public record Preferencia(String preferenceId, String initPoint, String sandboxInitPoint) {}

    //acreditado = true cuando Mercado Pago confirmo el pago y la factura quedo PAGADA
    public record Resultado(boolean acreditado, String estadoPago, Long facturaId, String mensaje) {}
}
