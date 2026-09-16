package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Reclamo;
import com.cooperativa.coop_servicios_backend.repositories.ReclamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReclamoService {
    @Autowired private ReclamoRepository repository;

    public List<Reclamo> obtenerTodos() { return repository.findAll(); }
    public Reclamo guardar(Reclamo reclamo) { return repository.save(reclamo); }
}
