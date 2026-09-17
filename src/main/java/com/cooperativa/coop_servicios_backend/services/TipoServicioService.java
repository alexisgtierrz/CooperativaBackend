package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.TipoServicio;
import com.cooperativa.coop_servicios_backend.repositories.TipoServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoServicioService {

    @Autowired
    private TipoServicioRepository repository;

    public List<TipoServicio> obtenerTodos() { return repository.findAll(); }

    public Optional<TipoServicio> obtenerPorId(Long id) { return repository.findById(id); }

    public TipoServicio guardar(TipoServicio tipoServicio) { return repository.save(tipoServicio); }

    public void eliminar(Long id) { repository.deleteById(id); }
}