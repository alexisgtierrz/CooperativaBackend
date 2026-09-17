package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Empleado;
import com.cooperativa.coop_servicios_backend.services.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService service;

    @GetMapping
    public List<Empleado> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Empleado crear(@RequestBody Empleado empleado) {
        return service.guardar(empleado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @RequestBody Empleado empleadoDetalles) {
        return service.obtenerPorId(id).map(empleadoExistente -> {
            empleadoExistente.setNombre(empleadoDetalles.getNombre());
            empleadoExistente.setApellido(empleadoDetalles.getApellido());
            empleadoExistente.setDni(empleadoDetalles.getDni());
            empleadoExistente.setTelefono(empleadoDetalles.getTelefono());
            empleadoExistente.setEspecialidad(empleadoDetalles.getEspecialidad());
            return ResponseEntity.ok(service.guardar(empleadoExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
