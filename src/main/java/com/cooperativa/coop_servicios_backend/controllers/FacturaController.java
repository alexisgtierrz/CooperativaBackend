package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Factura;
import com.cooperativa.coop_servicios_backend.DTOs.CobranzaResponse;
import com.cooperativa.coop_servicios_backend.DTOs.FacturaResponse;
import com.cooperativa.coop_servicios_backend.services.FacturaPagoService;
import com.cooperativa.coop_servicios_backend.services.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaService service;

    @Autowired
    private FacturaPagoService pagoService;

    //Facturas del usuario autenticado
    @GetMapping("/mis-facturas")
    public List<FacturaResponse> misFacturas(@RequestParam(required = false) String estado, Principal principal) {
        return pagoService.misFacturas(principal.getName(), estado);
    }

    //Cobranzas: clientes con servicios, facturas y deuda (solo administrador)
    @GetMapping("/cobranzas")
    public List<CobranzaResponse> cobranzas() {
        return pagoService.cobranzas();
    }

    @GetMapping
    public List<Factura> listarTodas() {
        return service.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Factura crear(@RequestBody Factura factura) {
        return service.guardar(factura);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
