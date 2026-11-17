package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.DTOs.CambioEstadoReclamoRequest;
import com.cooperativa.coop_servicios_backend.DTOs.NuevoReclamoRequest;
import com.cooperativa.coop_servicios_backend.DTOs.ReclamoResponse;
import com.cooperativa.coop_servicios_backend.services.ReclamoService;
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
@RequestMapping("/api/reclamos")
public class ReclamoController {

    @Autowired
    private ReclamoService service;

    //Cliente

    @PostMapping
    public ResponseEntity<ReclamoResponse> crear(Principal principal, @RequestBody NuevoReclamoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(principal.getName(), request));
    }

    @GetMapping("/mis-reclamos")
    public List<ReclamoResponse> misReclamos(Principal principal) {
        return service.misReclamos(principal.getName());
    }

    //Empleado

    @GetMapping("/sin-asignar")
    public List<ReclamoResponse> sinAsignar(Principal principal) {
        return service.sinAsignar(principal.getName());
    }

    @GetMapping("/asignados")
    public List<ReclamoResponse> asignados(Principal principal) {
        return service.asignados(principal.getName());
    }

    @PutMapping("/{id}/asignar")
    public ReclamoResponse asignar(@PathVariable Long id, Principal principal) {
        return service.asignar(id, principal.getName());
    }

    @PutMapping("/{id}/estado")
    public ReclamoResponse cambiarEstado(@PathVariable Long id, Principal principal,
                                         @RequestBody CambioEstadoReclamoRequest request) {
        return service.cambiarEstado(id, principal.getName(), request);
    }

    //Devuelve el motivo en el cuerpo ({"mensaje": "..."}) para que el front lo muestre
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarError(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensaje", ex.getReason() == null ? "Error" : ex.getReason()));
    }
}
