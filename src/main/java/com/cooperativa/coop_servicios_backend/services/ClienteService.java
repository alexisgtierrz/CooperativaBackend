package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Barrio;
import com.cooperativa.coop_servicios_backend.models.Cliente;
import com.cooperativa.coop_servicios_backend.models.Domicilio;
import com.cooperativa.coop_servicios_backend.repositories.BarrioRepository;
import com.cooperativa.coop_servicios_backend.repositories.ClienteRepository;
import com.cooperativa.coop_servicios_backend.repositories.DomicilioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired private DomicilioRepository domicilioRepository;

    public List<Cliente> obtenerTodos() { return repository.findAll(); }
    public Optional<Cliente> obtenerPorId(Long id) { return repository.findById(id); }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getDomicilio() == null || cliente.getDomicilio().getId() == null) {
            throw new RuntimeException("Error arquitectónico: El Cliente debe tener un Domicilio asignado de forma obligatoria.");
        }

        Domicilio domicilioReal = domicilioRepository.findById(cliente.getDomicilio().getId())
                .orElseThrow(() -> new RuntimeException("El domicilio especificado (" + cliente.getDomicilio().getId() + ") no existe en la base de datos."));

        cliente.setDomicilio(domicilioReal);

        return repository.save(cliente);
    }

    public void eliminar(Long id) { repository.deleteById(id); }
}