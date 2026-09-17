package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Pago;
import com.cooperativa.coop_servicios_backend.repositories.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRepository repository;

    public List<Pago> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Pago> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Pago guardar(Pago pago) {
        //Todo pago debe tener un Medio de Pago asignado
        if (pago.getMedioPago() == null || pago.getMedioPago().getId() == null) {
            throw new RuntimeException("Error teórico: El Pago debe realizarse a través de un Medio de Pago válido.");
        }

        //Todo pago debe imputarse a una Factura
        if (pago.getFactura() == null || pago.getFactura().getId() == null) {
            throw new RuntimeException("Error teórico: El Pago debe estar asociado a una Factura existente.");
        }


        return repository.save(pago);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
