package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Permiso;
import com.cooperativa.coop_servicios_backend.repositories.PermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermisoService {

    @Autowired
    private PermisoRepository repository;

    public List<Permiso> obtenerTodos() { return repository.findAll(); }

    public Optional<Permiso> obtenerPorId(Long id) { return repository.findById(id); }

    public Permiso guardar(Permiso permiso) { return repository.save(permiso); }

    public void eliminar(Long id) { repository.deleteById(id); }
}