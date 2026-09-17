package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Domicilio;
import com.cooperativa.coop_servicios_backend.repositories.DomicilioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DomicilioService {

    @Autowired
    private DomicilioRepository repository;

    public List<Domicilio> obtenerTodos() { return repository.findAll(); }
    public Optional<Domicilio> obtenerPorId(Long id) { return repository.findById(id); }
    public Domicilio guardar(Domicilio domicilio) {
        if (domicilio.getBarrio() == null || domicilio.getBarrio().getId() == null) {
            throw new RuntimeException("Error: El Domicilio debe pertenecer a un Barrio.");
        }
        return repository.save(domicilio);
    }    public void eliminar(Long id) { repository.deleteById(id); }
}