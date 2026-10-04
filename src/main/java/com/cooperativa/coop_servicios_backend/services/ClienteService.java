package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.Cliente;
import com.cooperativa.coop_servicios_backend.models.Domicilio;
import com.cooperativa.coop_servicios_backend.models.Perfil;
import com.cooperativa.coop_servicios_backend.models.Usuario;
import com.cooperativa.coop_servicios_backend.repositories.ClienteRepository;
import com.cooperativa.coop_servicios_backend.repositories.DomicilioRepository;
import com.cooperativa.coop_servicios_backend.repositories.PerfilRepository;
import com.cooperativa.coop_servicios_backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Cliente> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {

        // 1. MANEJO SEGURO DEL DOMICILIO
        if (cliente.getDomicilio() != null) {
            Domicilio domicilio = cliente.getDomicilio();
            if (domicilio.getId() != null) {
                Domicilio domicilioExistente = domicilioRepository.findById(domicilio.getId()).orElse(null);
                if (domicilioExistente != null) {
                    domicilioExistente.setCalle(domicilio.getCalle());
                    domicilioExistente.setNumero(domicilio.getNumero());
                    domicilioExistente.setPiso(domicilio.getPiso());
                    domicilioExistente.setDepartamento(domicilio.getDepartamento());
                    domicilioExistente.setObservaciones(domicilio.getObservaciones());
                    if (domicilio.getBarrio() != null) {
                        domicilioExistente.setBarrio(domicilio.getBarrio());
                    }
                    cliente.setDomicilio(domicilioRepository.save(domicilioExistente));
                } else {
                    domicilio.setId(null);
                    cliente.setDomicilio(domicilioRepository.save(domicilio));
                }
            } else {
                cliente.setDomicilio(domicilioRepository.save(domicilio));
            }
        }

        // 2. CREACIÓN AUTOMÁTICA DE USUARIO
        if (cliente.getId() == null && cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(cliente.getEmail());
            if (usuarioExistente.isEmpty()) {
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setEmail(cliente.getEmail());
                nuevoUsuario.setPassword(passwordEncoder.encode(cliente.getDni()));
                nuevoUsuario.setActivo(true);

                Perfil perfilCliente = perfilRepository.findById(2L).orElse(null);
                nuevoUsuario.setPerfil(perfilCliente);

                Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
                cliente.setUsuario(usuarioGuardado);
            } else {
                cliente.setUsuario(usuarioExistente.get());
            }
        }

        // 3. LÓGICA DE VENCIMIENTO, DOMICILIO Y VALIDACIÓN DE PLANES DUPLICADOS
        if (cliente.getSuscripciones() != null) {
            ZoneId zonaArgentina = ZoneId.of("America/Argentina/Buenos_Aires");

            boolean yaTieneInternetActivo = false;
            boolean yaTieneTvActivo = false;
            boolean yaTieneTelefoniaActivo = false;

            // A. Verificamos suscripciones activas que el cliente YA tenía guardadas en la BD
            if (cliente.getId() != null) {
                Optional<Cliente> clienteOpt = repository.findById(cliente.getId());
                if (clienteOpt.isPresent() && clienteOpt.get().getSuscripciones() != null) {
                    for (var subExistente : clienteOpt.get().getSuscripciones()) {
                        if (subExistente.getFechaBaja() == null && subExistente.getServicio() != null) {
                            String nombreServicio = subExistente.getServicio().getNombre() != null ? subExistente.getServicio().getNombre().toLowerCase() : "";

                            if (nombreServicio.contains("internet") || nombreServicio.contains("mega") || nombreServicio.contains("mb")) {
                                yaTieneInternetActivo = true;
                            }
                            if (nombreServicio.contains("tv") || nombreServicio.contains("television") || nombreServicio.contains("televisión") || nombreServicio.contains("digital") || nombreServicio.contains("canales")) {
                                yaTieneTvActivo = true;
                            }
                            if (nombreServicio.contains("telefono") || nombreServicio.contains("teléfono") || nombreServicio.contains("fija") || nombreServicio.contains("telefonia") || nombreServicio.contains("telefonía")) {
                                yaTieneTelefoniaActivo = true;
                            }
                        }
                    }
                }
            }

            // B. Recorremos las suscripciones que vienen en el formulario actual
            for (var sub : cliente.getSuscripciones()) {

                if (cliente.getDomicilio() != null) {
                    sub.setDomicilio(cliente.getDomicilio());
                }

                if (sub.getServicio() != null) {
                    String nombreServicio = sub.getServicio().getNombre() != null ? sub.getServicio().getNombre().toLowerCase() : "";

                    boolean esInternet = nombreServicio.contains("internet") || nombreServicio.contains("mega") || nombreServicio.contains("mb");
                    boolean esTv = nombreServicio.contains("tv") || nombreServicio.contains("television") || nombreServicio.contains("televisión") || nombreServicio.contains("digital") || nombreServicio.contains("canales");
                    boolean esTelefonia = nombreServicio.contains("telefono") || nombreServicio.contains("fija") || nombreServicio.contains("teléfono") || nombreServicio.contains("telefonia") || nombreServicio.contains("telefonía");

                    // Si es una suscripción NUEVA (sin ID)
                    if (sub.getId() == null) {

                        if (esInternet) {
                            if (yaTieneInternetActivo) {
                                throw new RuntimeException("El cliente ya cuenta con un plan de internet activo. Debe dar de baja el anterior antes de contratar uno nuevo.");
                            }
                            yaTieneInternetActivo = true;
                        }

                        if (esTv) {
                            if (yaTieneTvActivo) {
                                throw new RuntimeException("El cliente ya cuenta con un plan de televisión activo. Debe dar de baja el anterior antes de contratar uno nuevo.");
                            }
                            yaTieneTvActivo = true;
                        }

                        if (esTelefonia) {
                            if (yaTieneTelefoniaActivo) {
                                throw new RuntimeException("El cliente ya cuenta con una línea de telefonía activa. Debe dar de baja la anterior antes de contratar una nueva.");
                            }
                            yaTieneTelefoniaActivo = true;
                        }

                        sub.setFechaAlta(LocalDate.now(zonaArgentina));
                        sub.setFechaHasta(LocalDate.now(zonaArgentina).plusDays(30));
                    }
                }
            }
        }

        return repository.save(cliente);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}