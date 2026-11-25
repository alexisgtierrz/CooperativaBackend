package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.DTOs.PagoRequests;
import com.cooperativa.coop_servicios_backend.services.FacturaPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

//Cobro de facturas con Mercado Pago. Convive con PagoController
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/pagos")
public class PagoMercadoPagoController {

    @Autowired
    private FacturaPagoService service;

    //Crea la preferencia de Checkout Pro y devuelve init_point / sandbox_init_point
    @PostMapping("/crear-preferencia")
    public PagoRequests.Preferencia crearPreferencia(@RequestBody PagoRequests.CrearPreferencia request, Principal principal) {
        return service.crearPreferencia(principal.getName(), request.facturaId());
    }

    //Retorno del usuario (back_url success): valida el payment_id contra Mercado Pago y acredita la factura
    @PostMapping("/confirmar")
    public PagoRequests.Resultado confirmar(@RequestBody PagoRequests.Confirmar request, Principal principal) {
        return service.confirmarPago(request.paymentId(), principal.getName());
    }

    //Webhook de Mercado Pago (publico): solo se usa el id del pago, y el estado se consulta de nuevo a la API con nuestro token. Siempre responde 200
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam Map<String, String> params,
                                        @RequestBody(required = false) Map<String, Object> body) {
        try {
            String tipo = params.getOrDefault("type", params.getOrDefault("topic", ""));
            if (body != null && tipo.isBlank() && body.get("type") != null) tipo = String.valueOf(body.get("type"));
            if (!tipo.isBlank() && !tipo.equals("payment")) return ResponseEntity.ok().build();

            String paymentId = params.getOrDefault("data.id", params.get("id"));
            if (paymentId == null && body != null && body.get("data") instanceof Map<?, ?> data && data.get("id") != null) {
                paymentId = String.valueOf(data.get("id"));
            }
            if (paymentId != null) service.confirmarPago(paymentId, null);
        } catch (ResponseStatusException e) {
            System.err.println("[Webhook MP] " + e.getReason());
        } catch (Exception e) {
            System.err.println("[Webhook MP] Error: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
