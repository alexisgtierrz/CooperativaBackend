package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Estado;
import com.cooperativa.coop_servicios_backend.repositories.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoService {

    @Autowired
    private EstadoRepository repository;

    public List<Estado> obtenerTodos() { return repository.findAll(); }

    public Optional<Estado> obtenerPorId(Long id) { return repository.findById(id); }

    public Estado guardar(Estado estado) { return repository.save(estado); }

    public void eliminar(Long id) { repository.deleteById(id); }
}
