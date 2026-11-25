package com.cooperativa.coop_servicios_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

//Cliente minimo de la API REST de Mercado Pago (Checkout Pro)
@Component
public class MercadoPagoClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {};

    @Value("${mp.access-token:}")
    private String accessToken;

    private final RestClient rest = RestClient.builder().baseUrl("https://api.mercadopago.com").build();

    public Map<String, Object> crearPreferencia(Map<String, Object> cuerpo) {
        verificarConfigurado();
        try {
            return rest.post().uri("/checkout/preferences")
                    .header("Authorization", "Bearer " + accessToken.trim())
                    .body(cuerpo)
                    .retrieve().body(MAPA);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Mercado Pago rechazó la preferencia (" + e.getStatusCode().value() + "): " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo comunicar con Mercado Pago: " + e.getMessage());
        }
    }

    public Map<String, Object> obtenerPago(String paymentId) {
        verificarConfigurado();
        try {
            return rest.get().uri("/v1/payments/{id}", paymentId)
                    .header("Authorization", "Bearer " + accessToken.trim())
                    .retrieve().body(MAPA);
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Mercado Pago no devolvió el pago " + paymentId + " (" + e.getStatusCode().value() + ").");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo comunicar con Mercado Pago: " + e.getMessage());
        }
    }

    //Pagos aprobados asociados a una referencia externa (el id de la factura), mas recientes primero
    @SuppressWarnings("unchecked")
    public java.util.List<Map<String, Object>> buscarPagosAprobados(String externalReference) {
        verificarConfigurado();
        try {
            Map<String, Object> r = rest.get()
                    .uri("/v1/payments/search?external_reference={ref}&status=approved&sort=date_created&criteria=desc", externalReference)
                    .header("Authorization", "Bearer " + accessToken.trim())
                    .retrieve().body(MAPA);
            Object resultados = r == null ? null : r.get("results");
            return resultados instanceof java.util.List<?> l ? (java.util.List<Map<String, Object>>) l : java.util.List.of();
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Mercado Pago no devolvió los pagos de la factura " + externalReference + " (" + e.getStatusCode().value() + ").");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo comunicar con Mercado Pago: " + e.getMessage());
        }
    }

    private void verificarConfigurado() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Mercado Pago no está configurado: falta la variable de entorno MP_ACCESS_TOKEN.");
        }
    }
}
