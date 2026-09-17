package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.TipoIncidenteTecnico;
import com.cooperativa.coop_servicios_backend.repositories.TipoIncidenteTecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoIncidenteTecnicoService {

    @Autowired
    private TipoIncidenteTecnicoRepository repository;

    public List<TipoIncidenteTecnico> obtenerTodos() { return repository.findAll(); }

    public Optional<TipoIncidenteTecnico> obtenerPorId(Long id) { return repository.findById(id); }

    public TipoIncidenteTecnico guardar(TipoIncidenteTecnico tipoIncidente) { return repository.save(tipoIncidente); }

    public void eliminar(Long id) { repository.deleteById(id); }
}
