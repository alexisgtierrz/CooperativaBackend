package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Reclamo;
import com.cooperativa.coop_servicios_backend.services.ReclamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {

    @Autowired private ReclamoService service;

    @GetMapping
    public List<Reclamo> listarTodos() { return service.obtenerTodos(); }

    @PostMapping
    public Reclamo crear(@RequestBody Reclamo reclamo) { return service.guardar(reclamo); }
}