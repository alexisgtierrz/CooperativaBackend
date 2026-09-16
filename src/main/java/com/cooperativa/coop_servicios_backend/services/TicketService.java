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
    public Ticket guardar(Ticket reclamo) { return repository.save(reclamo); }
}
