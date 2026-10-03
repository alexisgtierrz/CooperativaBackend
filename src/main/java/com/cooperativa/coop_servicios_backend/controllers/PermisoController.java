package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Permiso;
import com.cooperativa.coop_servicios_backend.repositories.PermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    @Autowired
    private PermisoRepository repository;

    @GetMapping
    public List<Permiso> listarTodos() {
        return repository.findAll();
    }

    @PostMapping
    public Permiso crear(@RequestBody Permiso permiso) {
        return repository.save(permiso);
    }
}