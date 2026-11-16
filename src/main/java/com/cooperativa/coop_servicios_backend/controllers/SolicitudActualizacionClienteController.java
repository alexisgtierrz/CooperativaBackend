package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.DTOs.DatosClienteDto;
import com.cooperativa.coop_servicios_backend.DTOs.SolicitudActualizacionClienteResponse;
import com.cooperativa.coop_servicios_backend.models.EstadoSolicitud;
import com.cooperativa.coop_servicios_backend.services.SolicitudActualizacionClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/solicitudes-actualizacion")
public class SolicitudActualizacionClienteController {

    @Autowired
    private SolicitudActualizacionClienteService service;

    // ---- Cliente autenticado ----

    @PostMapping
    public ResponseEntity<SolicitudActualizacionClienteResponse> crear(Principal principal,
                                                                       @RequestBody DatosClienteDto datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(principal.getName(), datos));
    }

    @GetMapping("/mia")
    public ResponseEntity<SolicitudActualizacionClienteResponse> miUltimaSolicitud(Principal principal) {
        return service.ultimaDelCliente(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ---- Solo administrador (ver SecurityConfig) ----

    @GetMapping
    public List<SolicitudActualizacionClienteResponse> listar(@RequestParam(required = false) EstadoSolicitud estado) {
        return service.listar(estado);
    }

    @PostMapping("/{id}/confirmar")
    public SolicitudActualizacionClienteResponse confirmar(@PathVariable Long id, Principal principal,
                                                           @RequestBody(required = false) DatosClienteDto datosEditados) {
        return service.confirmar(id, principal.getName(), datosEditados);
    }

    @PostMapping("/{id}/rechazar")
    public SolicitudActualizacionClienteResponse rechazar(@PathVariable Long id, Principal principal) {
        return service.rechazar(id, principal.getName());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarError(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensaje", ex.getReason() == null ? "Error" : ex.getReason()));
    }
}
