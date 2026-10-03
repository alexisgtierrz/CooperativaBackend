package com.cooperativa.coop_servicios_backend.controllers;

import com.cooperativa.coop_servicios_backend.models.Cliente;
import com.cooperativa.coop_servicios_backend.repositories.ClienteRepository;
import com.cooperativa.coop_servicios_backend.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @Autowired
    private ClienteRepository clienteRepository;

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

    @GetMapping("/perfil-actual")
    public ResponseEntity<Object> obtenerPerfilActual(Principal principal) {
        String email = principal.getName(); // El email que viene dentro del token JWT

        // Buscamos si el cliente está asociado a ese usuario
        Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioEmail(email);

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("tipoUsuario", "CLIENTE");
            response.put("usuario", Map.of("email", email));
            response.put("perfil", cliente);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(404).body("Perfil de cliente no encontrado para el usuario actual.");
    }

    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente) {
        return service.guardar(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @RequestBody Cliente clienteDetalles) {
        return service.obtenerPorId(id).map(clienteExistente -> {
            // Actualizamos datos básicos
            clienteExistente.setNombre(clienteDetalles.getNombre());
            clienteExistente.setApellido(clienteDetalles.getApellido());
            clienteExistente.setDni(clienteDetalles.getDni());
            clienteExistente.setTelefono(clienteDetalles.getTelefono());
            clienteExistente.setEmail(clienteDetalles.getEmail());
            clienteExistente.setActivo(clienteDetalles.getActivo());

            if (clienteDetalles.getDomicilio() != null) {
                clienteExistente.setDomicilio(clienteDetalles.getDomicilio());
            }

            if (clienteDetalles.getUsuario() != null) {
                clienteExistente.setUsuario(clienteDetalles.getUsuario());
            }

            // Manejo de suscripciones
            if (clienteDetalles.getSuscripciones() != null) {

                java.util.Map<Long, com.cooperativa.coop_servicios_backend.models.Suscripcion> subsEntrantesMap = clienteDetalles.getSuscripciones().stream()
                        .filter(s -> s.getId() != null)
                        .collect(java.util.stream.Collectors.toMap(com.cooperativa.coop_servicios_backend.models.Suscripcion::getId, s -> s));

                // Recorremos las suscripciones que ya tenía el cliente en la base de datos
                for (com.cooperativa.coop_servicios_backend.models.Suscripcion subExistente : clienteExistente.getSuscripciones()) {
                    if (subExistente.getFechaBaja() == null && !subsEntrantesMap.containsKey(subExistente.getId())) {
                        subExistente.setFechaBaja(java.time.LocalDate.now());
                    }
                }

                for (com.cooperativa.coop_servicios_backend.models.Suscripcion subDetalle : clienteDetalles.getSuscripciones()) {
                    if (subDetalle.getId() == null) {
                        if (subDetalle.getFechaAlta() == null) {
                            subDetalle.setFechaAlta(java.time.LocalDate.now());
                        }
                        clienteExistente.getSuscripciones().add(subDetalle);
                    } else {
                        for (com.cooperativa.coop_servicios_backend.models.Suscripcion subExistente : clienteExistente.getSuscripciones()) {
                            if (subExistente.getId().equals(subDetalle.getId())) {
                                subExistente.setFechaBaja(null);
                            }
                        }
                    }
                }
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