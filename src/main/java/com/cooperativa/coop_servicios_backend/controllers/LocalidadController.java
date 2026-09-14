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

    @GetMapping
    public List<Localidad> listarTodas() {
        return service.obtenerTodas();
    }

    @PostMapping
    public Localidad crear(@RequestBody Localidad localidad) {
        return service.guardar(localidad);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Localidad> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Localidad> actualizar(@PathVariable Long id, @RequestBody Localidad localidadDetalles) {
        return service.obtenerPorId(id).map(localidadExistente -> {
            localidadExistente.setNombre(localidadDetalles.getNombre());
            localidadExistente.setCodigoPostal(localidadDetalles.getCodigoPostal());
            return ResponseEntity.ok(service.guardar(localidadExistente));
        }).orElse(ResponseEntity.notFound().build());
    }
}
