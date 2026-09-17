package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Suscripcion;
import com.cooperativa.coop_servicios_backend.repositories.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuscripcionService {

    @Autowired
    private SuscripcionRepository repository;

    public List<Suscripcion> obtenerTodas() {
        return repository.findAll();
    }

    public Optional<Suscripcion> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Suscripcion guardar(Suscripcion suscripcion) {
        if (suscripcion.getServicio() == null || suscripcion.getServicio().getId() == null) {
            throw new RuntimeException("Error: La suscripción debe estar asociada a un Servicio.");
        }
        if (suscripcion.getDomicilio() == null || suscripcion.getDomicilio().getId() == null) {
            throw new RuntimeException("Error: La suscripción debe estar asociada a un Domicilio.");
        }
        return repository.save(suscripcion);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
