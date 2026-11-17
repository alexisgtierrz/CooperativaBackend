package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Especialidad;
import com.cooperativa.coop_servicios_backend.repositories.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadService {

    @Autowired
    private EspecialidadRepository repository;

    public List<Especialidad> listar() {
        return repository.findAllByOrderByNombreAsc();
    }

    //Busca en el catalogo ignorando mayusculas, acentos y espacios de mas
    public Optional<Especialidad> buscar(String texto) {
        String clave = ReclamoService.clave(texto);
        if (clave.isEmpty()) return Optional.empty();
        return repository.findAll().stream()
                .filter(e -> ReclamoService.clave(e.getNombre()).equals(clave))
                .findFirst();
    }

    //Devuelve el nombre oficial de la especialidad o responde 400 si esta vacia
    public String validarYNormalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La especialidad es obligatoria.");
        }
        return buscar(texto)
                .map(Especialidad::getNombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La especialidad '" + texto.trim() + "' no pertenece al catálogo de puestos de la cooperativa."));
    }
}
