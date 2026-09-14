package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Barrio;
import com.cooperativa.coop_servicios_backend.services.BarrioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barrios")
public class BarrioController {

    @Autowired
    private BarrioService service;

    @GetMapping
    public List<Barrio> listarTodos() {
        return service.obtenerTodos();
    }

    @PostMapping
    public Barrio crear(@RequestBody Barrio barrio) {
        return service.guardar(barrio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barrio> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Barrio> actualizar(@PathVariable Long id, @RequestBody Barrio barrioDetalles) {
        return service.obtenerPorId(id).map(barrioExistente -> {
            barrioExistente.setNombre(barrioDetalles.getNombre());
            // Actualizamos también la localidad en caso de que el barrio cambie de zona
            barrioExistente.setLocalidad(barrioDetalles.getLocalidad());
            return ResponseEntity.ok(service.guardar(barrioExistente));
        }).orElse(ResponseEntity.notFound().build());
    }
}