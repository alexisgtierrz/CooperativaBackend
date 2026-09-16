package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Domicilio;
import com.cooperativa.coop_servicios_backend.services.DomicilioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/domicilios")
public class DomicilioController {

    @Autowired
    private DomicilioService service;

    @GetMapping
    public List<Domicilio> listarTodos() { return service.obtenerTodos(); }

    @PostMapping
    public Domicilio crear(@RequestBody Domicilio domicilio) { return service.guardar(domicilio); }
}
