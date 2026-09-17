package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.MedioPago;
import com.cooperativa.coop_servicios_backend.services.MedioPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/medios-pago")
public class MedioPagoController {

    @Autowired
    private MedioPagoService service;

    @GetMapping
    public List<MedioPago> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedioPago> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public MedioPago crear(@RequestBody MedioPago medioPago) {
        return service.guardar(medioPago);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedioPago> actualizar(@PathVariable Long id, @RequestBody MedioPago detalles) {
        return service.obtenerPorId(id).map(existente -> {
            existente.setNombre(detalles.getNombre());
            existente.setEsActivo(detalles.getEsActivo());
            return ResponseEntity.ok(service.guardar(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
