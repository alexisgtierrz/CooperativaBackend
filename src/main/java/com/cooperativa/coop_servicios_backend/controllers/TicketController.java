package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Ticket;
import com.cooperativa.coop_servicios_backend.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/reclamos")
public class TicketController {

    @Autowired private TicketService service;

    @GetMapping
    public List<Ticket> listarTodos() { return service.obtenerTodos(); }

    @PostMapping
    public Ticket crear(@RequestBody Ticket reclamo) { return service.guardar(reclamo); }
}