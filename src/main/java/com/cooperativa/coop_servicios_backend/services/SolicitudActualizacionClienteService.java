package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.DTOs.DatosClienteDto;
import com.cooperativa.coop_servicios_backend.DTOs.SolicitudActualizacionClienteResponse;
import com.cooperativa.coop_servicios_backend.models.*;
import com.cooperativa.coop_servicios_backend.repositories.BarrioRepository;
import com.cooperativa.coop_servicios_backend.repositories.ClienteRepository;
import com.cooperativa.coop_servicios_backend.repositories.SolicitudActualizacionClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudActualizacionClienteService {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    @Autowired
    private SolicitudActualizacionClienteRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private BarrioRepository barrioRepository;

    // ================= Cliente =================

    /** Crea una solicitud PENDIENTE para el cliente asociado al usuario autenticado. No modifica el cliente. */
    @Transactional
    public SolicitudActualizacionClienteResponse crear(String emailUsuario, DatosClienteDto datos) {
        Cliente cliente = clienteRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontro un cliente asociado a tu usuario."));

        if (repository.existsByClienteIdAndEstado(cliente.getId(), EstadoSolicitud.PENDIENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya tenes una solicitud de actualizacion pendiente de aprobacion.");
        }

        SolicitudActualizacionCliente solicitud = new SolicitudActualizacionCliente();
        solicitud.setCliente(cliente);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(LocalDateTime.now(ZONA));
        cargarDatos(solicitud, datos);

        return aRespuesta(repository.save(solicitud));
    }

    /** Ultima solicitud del cliente autenticado (cualquier estado), si existe. */
    @Transactional(readOnly = true)
    public Optional<SolicitudActualizacionClienteResponse> ultimaDelCliente(String emailUsuario) {
        return clienteRepository.findByUsuarioEmail(emailUsuario)
                .flatMap(c -> repository.findFirstByClienteIdOrderByFechaSolicitudDescIdDesc(c.getId()))
                .map(this::aRespuesta);
    }

    // ================= Administrador =================

    @Transactional(readOnly = true)
    public List<SolicitudActualizacionClienteResponse> listar(EstadoSolicitud estado) {
        List<SolicitudActualizacionCliente> lista = (estado == null)
                ? repository.findAllByOrderByFechaSolicitudDesc()
                : repository.findByEstadoOrderByFechaSolicitudDesc(estado);
        return lista.stream().map(this::aRespuesta).toList();
    }

    /**
     * Confirma la solicitud y actualiza el Cliente en la misma transaccion.
     * Si vienen datosEditados, el administrador corrige la propuesta antes de aprobar.
     */
    @Transactional
    public SolicitudActualizacionClienteResponse confirmar(Long id, String emailAdmin, DatosClienteDto datosEditados) {
        SolicitudActualizacionCliente solicitud = obtenerPendiente(id);

        if (datosEditados != null) {
            cargarDatos(solicitud, datosEditados);
        }

        aplicarAlCliente(solicitud);

        solicitud.setEstado(EstadoSolicitud.CONFIRMADA);
        solicitud.setFechaResolucion(LocalDateTime.now(ZONA));
        solicitud.setResueltaPor(emailAdmin);
        return aRespuesta(repository.save(solicitud));
    }

    @Transactional
    public SolicitudActualizacionClienteResponse rechazar(Long id, String emailAdmin) {
        SolicitudActualizacionCliente solicitud = obtenerPendiente(id);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaResolucion(LocalDateTime.now(ZONA));
        solicitud.setResueltaPor(emailAdmin);
        return aRespuesta(repository.save(solicitud));
    }

    // ================= Internos =================

    private SolicitudActualizacionCliente obtenerPendiente(Long id) {
        SolicitudActualizacionCliente solicitud = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La solicitud no existe."));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La solicitud ya fue resuelta (" + solicitud.getEstado() + ").");
        }
        return solicitud;
    }

    /** Valida y copia los datos propuestos a la solicitud. */
    private void cargarDatos(SolicitudActualizacionCliente solicitud, DatosClienteDto d) {
        if (d == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan los datos de la solicitud.");
        }
        String nombre = limpiar(d.nombre());
        String apellido = limpiar(d.apellido());
        if (nombre == null || apellido == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y el apellido son obligatorios.");
        }
        validarLargo(nombre, 100, "nombre");
        validarLargo(apellido, 100, "apellido");

        String telefono = limpiar(d.telefono());
        validarLargo(telefono, 50, "telefono");

        String calle = limpiar(d.calle());
        boolean hayDomicilio = calle != null || d.numero() != null || d.barrioId() != null;
        Barrio barrio = null;
        if (hayDomicilio) {
            if (calle == null || d.numero() == null || d.barrioId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Para actualizar el domicilio completa calle, numero y barrio.");
            }
            barrio = barrioRepository.findById(d.barrioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El barrio seleccionado no existe."));
        }

        solicitud.setNombre(nombre);
        solicitud.setApellido(apellido);
        solicitud.setTelefono(telefono);
        solicitud.setCalle(calle);
        solicitud.setNumero(hayDomicilio ? d.numero() : null);
        solicitud.setPiso(limpiar(d.piso()));
        solicitud.setDepartamento(limpiar(d.departamento()));
        solicitud.setObservaciones(limpiar(d.observaciones()));
        solicitud.setBarrio(barrio);
    }

    /** Copia los datos de la solicitud sobre el Cliente (y su Domicilio) administrados por la transaccion actual. */
    private void aplicarAlCliente(SolicitudActualizacionCliente s) {
        Cliente cliente = s.getCliente();
        cliente.setNombre(s.getNombre());
        cliente.setApellido(s.getApellido());
        cliente.setTelefono(s.getTelefono());

        // Si la solicitud no trae domicilio, el domicilio actual queda como esta
        if (s.getCalle() != null && s.getBarrio() != null) {
            Domicilio domicilio = cliente.getDomicilio();
            if (domicilio == null) {
                domicilio = new Domicilio();
                cliente.setDomicilio(domicilio); // cascade ALL lo persiste junto con el cliente
            }
            domicilio.setCalle(s.getCalle());
            domicilio.setNumero(s.getNumero());
            domicilio.setPiso(s.getPiso());
            domicilio.setDepartamento(s.getDepartamento());
            domicilio.setObservaciones(s.getObservaciones());
            domicilio.setBarrio(s.getBarrio());
        }
        clienteRepository.save(cliente);
    }

    private SolicitudActualizacionClienteResponse aRespuesta(SolicitudActualizacionCliente s) {
        Cliente c = s.getCliente();
        Domicilio dom = c.getDomicilio();

        DatosClienteDto actual = new DatosClienteDto(
                c.getNombre(), c.getApellido(), c.getTelefono(),
                dom != null ? dom.getCalle() : null,
                dom != null ? dom.getNumero() : null,
                dom != null ? dom.getPiso() : null,
                dom != null ? dom.getDepartamento() : null,
                dom != null ? dom.getObservaciones() : null,
                dom != null && dom.getBarrio() != null ? dom.getBarrio().getId() : null,
                dom != null && dom.getBarrio() != null ? dom.getBarrio().getNombre() : null,
                dom != null && dom.getBarrio() != null && dom.getBarrio().getLocalidad() != null ? dom.getBarrio().getLocalidad().getId() : null,
                dom != null && dom.getBarrio() != null && dom.getBarrio().getLocalidad() != null ? dom.getBarrio().getLocalidad().getNombre() : null
        );

        Barrio b = s.getBarrio();
        DatosClienteDto propuesto = new DatosClienteDto(
                s.getNombre(), s.getApellido(), s.getTelefono(),
                s.getCalle(), s.getNumero(), s.getPiso(), s.getDepartamento(), s.getObservaciones(),
                b != null ? b.getId() : null,
                b != null ? b.getNombre() : null,
                b != null && b.getLocalidad() != null ? b.getLocalidad().getId() : null,
                b != null && b.getLocalidad() != null ? b.getLocalidad().getNombre() : null
        );

        return new SolicitudActualizacionClienteResponse(
                s.getId(), s.getEstado().name(), s.getFechaSolicitud(), s.getFechaResolucion(), s.getResueltaPor(),
                c.getId(), c.getDni(), actual, propuesto);
    }

    private static String limpiar(String valor) {
        if (valor == null) return null;
        String v = valor.trim();
        return v.isEmpty() ? null : v;
    }

    private static void validarLargo(String valor, int max, String campo) {
        if (valor != null && valor.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El campo " + campo + " no puede superar los " + max + " caracteres.");
        }
    }
}
