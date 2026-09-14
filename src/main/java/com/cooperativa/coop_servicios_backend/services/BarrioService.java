package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Barrio;
import com.cooperativa.coop_servicios_backend.repositories.BarrioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BarrioService {

    @Autowired
    private BarrioRepository repository;

    public List<Barrio> obtenerTodos() { return repository.findAll(); }

    public Optional<Barrio> obtenerPorId(Long id) { return repository.findById(id); }

    public Barrio guardar(Barrio barrio) { return repository.save(barrio); }

    public void eliminar(Long id) { repository.deleteById(id); }
}
