package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.TipoServicio;
import com.cooperativa.coop_servicios_backend.services.TipoServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tipos-servicio")
public class TipoServicioController {

    @Autowired
    private TipoServicioService service;

    @GetMapping
    public List<TipoServicio> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoServicio> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TipoServicio crear(@RequestBody TipoServicio tipoServicio) {
        return service.guardar(tipoServicio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoServicio> actualizar(@PathVariable Long id, @RequestBody TipoServicio detalles) {
        return service.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            return ResponseEntity.ok(service.guardar(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
