package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.CambioEstado;
import com.cooperativa.coop_servicios_backend.repositories.CambioEstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CambioEstadoService {

    @Autowired
    private CambioEstadoRepository repository;

    public List<CambioEstado> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<CambioEstado> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public CambioEstado guardar(CambioEstado cambioEstado) {
        //Debe estar asociado a un Estado
        if (cambioEstado.getEstado() == null || cambioEstado.getEstado().getId() == null) {
            throw new RuntimeException("Error teórico: El Cambio de Estado debe tener un Estado asociado.");
        }

        //Debe registrar qué empleado lo realizó
        if (cambioEstado.getEmpleado() == null || cambioEstado.getEmpleado().getId() == null) {
            throw new RuntimeException("Error de auditoría: El Cambio de Estado debe registrar el Empleado responsable.");
        }

        return repository.save(cambioEstado);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
