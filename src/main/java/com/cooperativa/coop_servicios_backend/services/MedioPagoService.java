package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.MedioPago;
import com.cooperativa.coop_servicios_backend.repositories.MedioPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedioPagoService {

    @Autowired
    private MedioPagoRepository repository;

    public List<MedioPago> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<MedioPago> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public MedioPago guardar(MedioPago medioPago) {
        return repository.save(medioPago);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
