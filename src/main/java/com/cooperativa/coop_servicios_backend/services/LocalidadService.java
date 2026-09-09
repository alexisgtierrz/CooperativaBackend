package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Localidad;
import com.cooperativa.coop_servicios_backend.repositories.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocalidadService {

    @Autowired
    private LocalidadRepository repository;

    // Metodo para consultar todas las localidades
    public List<Localidad> obtenerTodas() {
        return repository.findAll();
    }

    // Metodo para consultar una localidad específica por ID
    public Optional<Localidad> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    // Metodo para registrar o modificar una localidad
    public Localidad guardar(Localidad localidad) {
        return repository.save(localidad);
    }

    // Metodo para eliminar una localidad
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}