package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Localidad;
import com.cooperativa.coop_servicios_backend.services.LocalidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localidades")
public class LocalidadController {

    @Autowired
    private LocalidadService service;

    // Endpoint para obtener la lista de localidades (GET)
    @GetMapping
    public List<Localidad> listarTodas() {
        return service.obtenerTodas();
    }

    // Endpoint para crear una nueva localidad (POST)
    @PostMapping
    public Localidad crear(@RequestBody Localidad localidad) {
        return service.guardar(localidad);
    }

    // Endpoint para eliminar una localidad (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
