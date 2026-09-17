package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Empleado;
import com.cooperativa.coop_servicios_backend.repositories.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository repository;

    public List<Empleado> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Empleado> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Empleado guardar(Empleado empleado) {
        if (empleado.getUsuario() == null) {
            throw new RuntimeException("Error: El Empleado debe tener un Usuario asociado.");
        }
        return repository.save(empleado);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
