package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Barrio;
import com.cooperativa.coop_servicios_backend.models.Cliente;
import com.cooperativa.coop_servicios_backend.repositories.BarrioRepository;
import com.cooperativa.coop_servicios_backend.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private BarrioRepository barrioRepository;

    public List<Cliente> obtenerTodos() { return repository.findAll(); }
    public Optional<Cliente> obtenerPorId(Long id) { return repository.findById(id); }

    public Cliente guardar(Cliente cliente) {
        //Verificamos que el barrio exista y lo cargamos completo
        if (cliente.getBarrio() != null && cliente.getBarrio().getId() != null) {
            Barrio barrioReal = barrioRepository.findById(cliente.getBarrio().getId())
                    .orElseThrow(() -> new RuntimeException("El barrio especificado no existe"));
            cliente.setBarrio(barrioReal);
        }
        return repository.save(cliente);
    }

    public void eliminar(Long id) { repository.deleteById(id); }
}