package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Recorrido;
import com.cooperativa.coop_servicios_backend.repositories.RecorridoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecorridoService {

    @Autowired
    private RecorridoRepository repository;

    public List<Recorrido> obtenerTodos() { return repository.findAll(); }
    public Optional<Recorrido> obtenerPorId(Long id) { return repository.findById(id); }

    public Recorrido guardar(Recorrido recorrido) {
        if (recorrido.getDomicilio() == null || recorrido.getDomicilio().getId() == null) {
            throw new RuntimeException("Error teórico: El Recorrido debe tener un Domicilio asignado.");
        }

        if (recorrido.getDetalles() == null || recorrido.getDetalles().isEmpty()) {
            throw new RuntimeException("Error de agregación: Un Recorrido debe tener al menos un Detalle de Recorrido.");
        }

        return repository.save(recorrido);
    }

    public void eliminar(Long id) { repository.deleteById(id); }
}
