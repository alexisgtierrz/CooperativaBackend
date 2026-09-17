package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.PlanDeVisita;
import com.cooperativa.coop_servicios_backend.repositories.PlanDeVisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanDeVisitaService {

    @Autowired
    private PlanDeVisitaRepository repository;

    public List<PlanDeVisita> obtenerTodos() { return repository.findAll(); }
    public Optional<PlanDeVisita> obtenerPorId(Long id) { return repository.findById(id); }

    public PlanDeVisita guardar(PlanDeVisita plan) {
        if (plan.getRecorridos() == null || plan.getRecorridos().isEmpty()) {
            throw new RuntimeException("Error teórico: El Plan de Visita debe contener al menos un Recorrido.");
        }

        return repository.save(plan);
    }

    public void eliminar(Long id) { repository.deleteById(id); }
}
