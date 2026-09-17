package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Ticket;
import com.cooperativa.coop_servicios_backend.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService service;

    @GetMapping
    public List<Ticket> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ticket crear(@RequestBody Ticket ticket) {
        return service.guardar(ticket);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> actualizar(@PathVariable Long id, @RequestBody Ticket ticketDetalles) {
        return service.obtenerPorId(id).map(ticketExistente -> {
            ticketExistente.setDescripcion(ticketDetalles.getDescripcion());
            ticketExistente.setCategoria(ticketDetalles.getCategoria());

            // Mantenemos la composición con el Incidente Técnico
            if (ticketDetalles.getIncidenteTecnico() != null) {
                ticketExistente.setIncidenteTecnico(ticketDetalles.getIncidenteTecnico());
            }

            // Clave: Permitir la actualización de los Cambios de Estado
            if (ticketDetalles.getHistorialEstados() != null) {
                ticketExistente.getHistorialEstados().clear();
                ticketExistente.getHistorialEstados().addAll(ticketDetalles.getHistorialEstados());
            }

            return ResponseEntity.ok(service.guardar(ticketExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}