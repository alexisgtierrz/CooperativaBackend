package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.PlanDeVisita;
import com.cooperativa.coop_servicios_backend.services.PlanDeVisitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/planes-visita")
public class PlanDeVisitaController {

    @Autowired private PlanDeVisitaService service;

    @GetMapping
    public List<PlanDeVisita> listarTodos() { return service.obtenerTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<PlanDeVisita> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PlanDeVisita crear(@RequestBody PlanDeVisita plan) { return service.guardar(plan); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
