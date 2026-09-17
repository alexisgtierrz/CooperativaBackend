package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Recorrido;
import com.cooperativa.coop_servicios_backend.services.RecorridoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/recorridos")
public class RecorridoController {

    @Autowired private RecorridoService service;

    @GetMapping
    public List<Recorrido> listarTodos() { return service.obtenerTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<Recorrido> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Recorrido crear(@RequestBody Recorrido recorrido) { return service.guardar(recorrido); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
