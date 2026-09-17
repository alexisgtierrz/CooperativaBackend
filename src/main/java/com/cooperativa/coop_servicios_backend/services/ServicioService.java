package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Servicio;
import com.cooperativa.coop_servicios_backend.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository repository;

    public List<Servicio> obtenerTodos() { return repository.findAll(); }
    public Optional<Servicio> obtenerPorId(Long id) { return repository.findById(id); }
    public Servicio guardar(Servicio servicio) {
        if (servicio.getTipoServicio() == null || servicio.getTipoServicio().getId() == null) {
            throw new RuntimeException("Error: El Servicio debe tener un Tipo de Servicio asignado.");
        }
        if (servicio.getTarifa() == null || servicio.getTarifa().getId() == null) {
            throw new RuntimeException("Error: El Servicio debe tener una Tarifa asignada.");
        }
        return repository.save(servicio);
    }    public void eliminar(Long id) { repository.deleteById(id); }
}
