package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Cliente;
import com.cooperativa.coop_servicios_backend.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping
    public List<Cliente> listarTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente) {
        return service.guardar(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @RequestBody Cliente clienteDetalles) {
        return service.obtenerPorId(id).map(clienteExistente -> {
            clienteExistente.setNombre(clienteDetalles.getNombre());
            clienteExistente.setApellido(clienteDetalles.getApellido());
            clienteExistente.setDni(clienteDetalles.getDni());
            clienteExistente.setTelefono(clienteDetalles.getTelefono());
            clienteExistente.setEmail(clienteDetalles.getEmail());
            clienteExistente.setActivo(clienteDetalles.getActivo());
            clienteExistente.setDomicilio(clienteDetalles.getDomicilio());
            if (clienteDetalles.getSuscripciones() != null) {
                clienteExistente.getSuscripciones().clear();
                clienteExistente.getSuscripciones().addAll(clienteDetalles.getSuscripciones());
            }
            return ResponseEntity.ok(service.guardar(clienteExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}