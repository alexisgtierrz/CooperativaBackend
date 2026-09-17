package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Estado;
import com.cooperativa.coop_servicios_backend.services.EstadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/estados")
public class EstadoController {

    @Autowired
    private EstadoService service;

    @GetMapping
    public List<Estado> listarTodos() { return service.obtenerTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<Estado> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Estado crear(@RequestBody Estado estado) { return service.guardar(estado); }

    @PutMapping("/{id}")
    public ResponseEntity<Estado> actualizar(@PathVariable Long id, @RequestBody Estado detalles) {
        return service.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            existente.setDescripcion(detalles.getDescripcion());
            return ResponseEntity.ok(service.guardar(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
