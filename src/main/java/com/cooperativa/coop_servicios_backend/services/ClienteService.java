package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.models.*;
import com.cooperativa.coop_servicios_backend.repositories.*;
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

    @Autowired
    private ServicioRepository servicioRepository;

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

        // 3. LÓGICA DE VENCIMIENTO, DOMICILIO Y VALIDACIÓN
        if (cliente.getSuscripciones() != null) {
            ZoneId zonaArgentina = ZoneId.of("America/Argentina/Buenos_Aires");

            boolean tieneInternet = false;
            boolean tieneTv = false;
            boolean tieneTelefonia = false;

            for (var sub : cliente.getSuscripciones()) {

                if (cliente.getDomicilio() != null) {
                    sub.setDomicilio(cliente.getDomicilio());
                }

                if (sub.getServicio() != null && sub.getServicio().getId() != null) {
                    Long idServ = sub.getServicio().getId();

                    Servicio servicioReal = servicioRepository.findById(idServ)
                            .orElseThrow(() -> new RuntimeException("El servicio seleccionado no existe"));
                    sub.setServicio(servicioReal);

                    // Validamos que no haya duplicados en la lista que se va a guardar
                    if (idServ == 1L || idServ == 2L || idServ == 3L) {
                        if (tieneInternet) throw new RuntimeException("No se puede tener más de un plan de Internet a la vez.");
                        tieneInternet = true;
                    }
                    else if (idServ == 4L) {
                        if (tieneTv) throw new RuntimeException("No se puede tener más de un plan de Televisión a la vez.");
                        tieneTv = true;
                    }
                    else if (idServ == 5L) {
                        if (tieneTelefonia) throw new RuntimeException("No se puede tener más de una línea de Telefonía a la vez.");
                        tieneTelefonia = true;
                    }

                    // Si es una suscripción NUEVA (aún no tiene ID asignado), le generamos las fechas
                    if (sub.getId() == null) {
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