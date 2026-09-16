package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Servicio;
import com.cooperativa.coop_servicios_backend.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    @Autowired
    private ServicioService service;

    @GetMapping
    public List<Servicio> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Servicio crear(@RequestBody Servicio servicio) {
        return service.guardar(servicio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> actualizar(@PathVariable Long id, @RequestBody Servicio detalles) {
        return service.obtenerPorId(id).map(servicioExistente -> {
            servicioExistente.setNombre(detalles.getNombre());
            servicioExistente.setDescripcion(detalles.getDescripcion());
            servicioExistente.setActivo(detalles.getActivo());
            return ResponseEntity.ok(service.guardar(servicioExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}