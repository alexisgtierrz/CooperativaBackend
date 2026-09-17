package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Tarifa;
import com.cooperativa.coop_servicios_backend.repositories.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository repository;

    public List<Tarifa> obtenerTodas() { return repository.findAll(); }

    public Optional<Tarifa> obtenerPorId(Long id) { return repository.findById(id); }

    public Tarifa guardar(Tarifa tarifa) { return repository.save(tarifa); }

    public void eliminar(Long id) { repository.deleteById(id); }
}
