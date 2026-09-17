package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Ticket;
import com.cooperativa.coop_servicios_backend.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketService {
    @Autowired private TicketRepository repository;

    public List<Ticket> obtenerTodos() { return repository.findAll(); }
    public Ticket guardar(Ticket ticket) {
        if (ticket.getIncidenteTecnico() == null) {
            throw new RuntimeException("Error: Un Ticket debe contener un Incidente Técnico al ser creado.");
        }

        if (ticket.getIncidenteTecnico().getTipoIncidente() == null || ticket.getIncidenteTecnico().getTipoIncidente().getId() == null) {
            throw new RuntimeException("Error: El Incidente Técnico debe tener un Tipo de Incidente asignado.");
        }

        return repository.save(ticket);
    }
}
