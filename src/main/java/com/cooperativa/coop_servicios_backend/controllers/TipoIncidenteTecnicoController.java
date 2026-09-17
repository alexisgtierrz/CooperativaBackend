package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.TipoIncidenteTecnico;
import com.cooperativa.coop_servicios_backend.services.TipoIncidenteTecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tipos-incidente")
public class TipoIncidenteTecnicoController {

    @Autowired
    private TipoIncidenteTecnicoService service;

    @GetMapping
    public List<TipoIncidenteTecnico> listarTodos() { return service.obtenerTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<TipoIncidenteTecnico> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TipoIncidenteTecnico crear(@RequestBody TipoIncidenteTecnico tipo) { return service.guardar(tipo); }

    @PutMapping("/{id}")
    public ResponseEntity<TipoIncidenteTecnico> actualizar(@PathVariable Long id, @RequestBody TipoIncidenteTecnico detalles) {
        return service.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            existente.setGravedad(detalles.getGravedad());
            return ResponseEntity.ok(service.guardar(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
