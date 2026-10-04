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

    public Cliente guardar(Cliente cliente) {

        // 1. MANEJO DEL DOMICILIO
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
                    cliente.setDomicilio(domicilioExistente);
                } else {
                    domicilio.setId(null);
                    cliente.setDomicilio(domicilioRepository.save(domicilio));
                }
            } else {
                cliente.setDomicilio(domicilioRepository.save(domicilio));
            }
        }

        // 2. CREACIÓN AUTOMÁTICA DE USUARIO (Solo al registrar un nuevo cliente)
        if (cliente.getId() == null && cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {

            // Verificamos si ya existe un usuario con este email en la base de datos
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(cliente.getEmail());

            if (usuarioExistente.isEmpty()) {
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setEmail(cliente.getEmail());

                // Encriptamos el DNI y lo establecemos como la contraseña por defecto
                nuevoUsuario.setPassword(passwordEncoder.encode(cliente.getDni()));
                nuevoUsuario.setActivo(true);

                // Asignamos el perfil de "Cliente"
                Perfil perfilCliente = perfilRepository.findById(2L).orElse(null);
                nuevoUsuario.setPerfil(perfilCliente);

                // Guardamos el usuario y lo asignamos al nuevo cliente
                Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
                cliente.setUsuario(usuarioGuardado);
            } else {
                // Si el email ya tenía cuenta de usuario, lo vinculamos
                cliente.setUsuario(usuarioExistente.get());
            }
        }

        // 3. LÓGICA DE VENCIMIENTO DE SUSCRIPCIONES
        if (cliente.getSuscripciones() != null) {
            // Le decimos a Java que use explícitamente el huso horario de Argentina
            ZoneId zonaArgentina = ZoneId.of("America/Argentina/Buenos_Aires");

            for (var sub : cliente.getSuscripciones()) {
                if (sub.getId() == null) {
                    sub.setFechaAlta(LocalDate.now(zonaArgentina));
                    sub.setFechaHasta(LocalDate.now(zonaArgentina).plusMonths(1));
                }
            }
        }

        return repository.save(cliente);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}