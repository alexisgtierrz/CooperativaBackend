package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.IncidenteTecnico;
import com.cooperativa.coop_servicios_backend.services.IncidenteTecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/incidentes")
public class IncidenteTecnicoController {

    @Autowired
    private IncidenteTecnicoService service;

    @GetMapping
    public List<IncidenteTecnico> listarTodos() { return service.obtenerTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenteTecnico> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
