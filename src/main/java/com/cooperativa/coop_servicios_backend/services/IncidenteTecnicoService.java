package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.IncidenteTecnico;
import com.cooperativa.coop_servicios_backend.repositories.IncidenteTecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidenteTecnicoService {

    @Autowired
    private IncidenteTecnicoRepository repository;

    public List<IncidenteTecnico> obtenerTodos() { return repository.findAll(); }
    public Optional<IncidenteTecnico> obtenerPorId(Long id) { return repository.findById(id); }
    public IncidenteTecnico guardar(IncidenteTecnico incidente) { return repository.save(incidente); }
    public void eliminar(Long id) { repository.deleteById(id); }
}
