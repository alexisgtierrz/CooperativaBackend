package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Factura;
import com.cooperativa.coop_servicios_backend.repositories.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository repository;

    public List<Factura> obtenerTodas() {
        return repository.findAll();
    }

    public Optional<Factura> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Factura guardar(Factura factura) {
        //Asociación obligatoria con Cliente
        if (factura.getCliente() == null || factura.getCliente().getId() == null) {
            throw new RuntimeException("Error teórico: La Factura debe estar asociada a un Cliente.");
        }

        if (factura.getDetalles() == null || factura.getDetalles().isEmpty()) {
            throw new RuntimeException("Error de composición: Una Factura no puede existir sin Detalles de Factura.");
        }

        //Todo detalle debe tener una suscripcion asignada
        factura.getDetalles().forEach(detalle -> {
            if (detalle.getSuscripcion() == null || detalle.getSuscripcion().getId() == null) {
                throw new RuntimeException("Error teórico: Cada Detalle de Factura debe vincularse a una Suscripción.");
            }
        });

        return repository.save(factura);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
