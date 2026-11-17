package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Especialidad;
import com.cooperativa.coop_servicios_backend.services.EspecialidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    @Autowired
    private EspecialidadService service;

    //Puestos de trabajo/especialidades, ordenados por nombre
    @GetMapping
    public List<Especialidad> listar() {
        return service.listar();
    }
}
