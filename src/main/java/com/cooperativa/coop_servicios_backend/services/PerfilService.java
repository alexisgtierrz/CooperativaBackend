package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Perfil;
import com.cooperativa.coop_servicios_backend.repositories.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository repository;

    public List<Perfil> obtenerTodos() { return repository.findAll(); }
    public Optional<Perfil> obtenerPorId(Long id) { return repository.findById(id); }
    public Perfil guardar(Perfil perfil) { return repository.save(perfil); }
    public void eliminar(Long id) { repository.deleteById(id); }
}
