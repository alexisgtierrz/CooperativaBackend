package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.CambioEstado;
import com.cooperativa.coop_servicios_backend.services.CambioEstadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/cambios-estado")
public class CambioEstadoController {

    @Autowired
    private CambioEstadoService service;

    @GetMapping
    public List<CambioEstado> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CambioEstado> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CambioEstado crear(@RequestBody CambioEstado cambioEstado) {
        return service.guardar(cambioEstado);
    }
}