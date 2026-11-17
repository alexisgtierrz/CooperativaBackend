package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.DTOs.CambioEstadoReclamoRequest;
import com.cooperativa.coop_servicios_backend.DTOs.EmpleadoPerfilResponse;
import com.cooperativa.coop_servicios_backend.DTOs.NuevoReclamoRequest;
import com.cooperativa.coop_servicios_backend.DTOs.ReclamoResponse;
import com.cooperativa.coop_servicios_backend.models.*;
import com.cooperativa.coop_servicios_backend.repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

//El cliente crea y sigue sus reclamos; el empleado toma los de su especialidad y los resuelve
@Service
public class ReclamoService {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String EN_PROCESO = "EN_PROCESO";
    public static final String RESUELTO = "RESUELTO";

    static final String NOMBRE_PENDIENTE = "Pendiente";
    static final String NOMBRE_EN_PROCESO = "En proceso";
    static final String NOMBRE_RESUELTO = "Resuelto";

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final int MAX_DESCRIPCION = 255;
    private static final int MAX_OBSERVACIONES = 1000;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private TipoIncidenteTecnicoRepository tipoRepository;
    @Autowired
    private EstadoRepository estadoRepository;
    @PersistenceContext
    private EntityManager entityManager;

    //Cliente
    @Transactional
    public ReclamoResponse crear(String emailUsuario, NuevoReclamoRequest req) {
        Cliente cliente = clientePorEmail(emailUsuario);

        if (req == null || req.tipoIncidenteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elegi el tipo de incidente.");
        }
        String descripcion = req.descripcion() == null ? "" : req.descripcion().trim();
        if (descripcion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta que paso: la descripcion es obligatoria.");
        }
        if (descripcion.length() > MAX_DESCRIPCION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripcion no puede superar los " + MAX_DESCRIPCION + " caracteres.");
        }
        TipoIncidenteTecnico tipo = tipoRepository.findById(req.tipoIncidenteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de incidente no existe."));

        IncidenteTecnico incidente = new IncidenteTecnico();
        incidente.setTipoIncidente(tipo);
        incidente.setRequiereVisita(false);

        Ticket ticket = new Ticket();
        ticket.setFechaCreacion(LocalDate.now(ZONA));
        ticket.setDescripcion(descripcion);
        ticket.setCategoria(tipo.getNombre());
        ticket.setIncidenteTecnico(incidente);
        ticket.setEmpleado(null);
        ticket.setHistorialEstados(new ArrayList<>());
        registrarEstado(ticket, estado(NOMBRE_PENDIENTE), null);

        ticket = ticketRepository.save(ticket);

        //La relacion Cliente -> tickets es la dueña de la columna cliente_id
        if (cliente.getTickets() == null) cliente.setTickets(new ArrayList<>());
        cliente.getTickets().add(ticket);
        clienteRepository.save(cliente);

        return aRespuesta(ticket, null);
    }

    @Transactional(readOnly = true)
    public List<ReclamoResponse> misReclamos(String emailUsuario) {
        Cliente cliente = clientePorEmail(emailUsuario);
        List<Ticket> tickets = cliente.getTickets() == null ? List.of() : new ArrayList<>(cliente.getTickets());
        tickets.sort(Comparator.comparing(Ticket::getId).reversed());
        return tickets.stream().map(t -> aRespuesta(t, null)).toList();
    }

    //Empleado

    @Transactional(readOnly = true)
    public Optional<EmpleadoPerfilResponse> perfilEmpleado(String emailUsuario) {
        return empleadoRepository.findByUsuarioEmail(emailUsuario)
                .map(e -> new EmpleadoPerfilResponse(e.getId(), e.getNombre(), e.getApellido(), e.getLegajo(), e.getEspecialidad()));
    }

    //Reclamos sin tecnico cuyo tipo de incidente pertenece a la especialidad del empleado
    @Transactional(readOnly = true)
    public List<ReclamoResponse> sinAsignar(String emailUsuario) {
        Empleado empleado = empleadoPorEmail(emailUsuario);
        if (clave(empleado.getEspecialidad()).isEmpty()) return List.of();

        List<Ticket> tickets = ticketRepository.findByEmpleadoIsNullOrderByIdAsc().stream()
                .filter(t -> esDeSuArea(empleado, t))
                .toList();
        return aRespuestas(tickets, true);
    }

    @Transactional(readOnly = true)
    public List<ReclamoResponse> asignados(String emailUsuario) {
        Empleado empleado = empleadoPorEmail(emailUsuario);
        return aRespuestas(ticketRepository.findByEmpleadoIdOrderByIdDesc(empleado.getId()), true);
    }

    //El empleado toma el reclamo. La fila se bloquea, asi que solo uno puede quedarselo
    @Transactional
    public ReclamoResponse asignar(Long ticketId, String emailUsuario) {
        Empleado empleado = empleadoPorEmail(emailUsuario);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El reclamo no existe."));

        if (ticket.getEmpleado() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este reclamo ya fue tomado por otro companero.");
        }
        if (clave(empleado.getEspecialidad()).isEmpty() || !esDeSuArea(empleado, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este reclamo no corresponde a tu especialidad.");
        }

        ticket.setEmpleado(empleado);
        registrarEstado(ticket, estado(NOMBRE_EN_PROCESO), empleado);
        return aRespuestas(List.of(ticketRepository.save(ticket)), true).get(0);
    }

    //Cambia el estado (EN_PROCESO / RESUELTO) y guarda la nota tecnica. Solo el tecnico asignado
    @Transactional
    public ReclamoResponse cambiarEstado(Long ticketId, String emailUsuario, CambioEstadoReclamoRequest req) {
        Empleado empleado = empleadoPorEmail(emailUsuario);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El reclamo no existe."));

        if (ticket.getEmpleado() == null || !ticket.getEmpleado().getId().equals(empleado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el tecnico asignado puede modificar este reclamo.");
        }

        String nuevo = req == null || req.estado() == null ? "" : req.estado().trim().toUpperCase(Locale.ROOT);
        if (!nuevo.equals(EN_PROCESO) && !nuevo.equals(RESUELTO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado invalido. Usa EN_PROCESO o RESUELTO.");
        }
        String actual = codigoEstado(ticket);
        if (actual.equals(RESUELTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El reclamo ya esta resuelto.");
        }

        String observaciones = req.observaciones() == null ? "" : req.observaciones().trim();
        if (observaciones.length() > MAX_OBSERVACIONES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La observacion no puede superar los " + MAX_OBSERVACIONES + " caracteres.");
        }
        if (nuevo.equals(RESUELTO) && observaciones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carga una observacion tecnica de la solucion para resolver el reclamo.");
        }
        if (!observaciones.isEmpty()) {
            ticket.setObservacionesResolucion(observaciones);
        }

        if (!nuevo.equals(actual)) {
            registrarEstado(ticket, estado(nuevo.equals(RESUELTO) ? NOMBRE_RESUELTO : NOMBRE_EN_PROCESO), empleado);
        }
        return aRespuestas(List.of(ticketRepository.save(ticket)), true).get(0);
    }

    //Internos
    private Cliente clientePorEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontro una ficha de asociado para tu usuario."));
    }

    private Empleado empleadoPorEmail(String email) {
        return empleadoRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Tu usuario no corresponde a un empleado."));
    }

    private boolean esDeSuArea(Empleado empleado, Ticket ticket) {
        TipoIncidenteTecnico tipo = ticket.getIncidenteTecnico() == null ? null : ticket.getIncidenteTecnico().getTipoIncidente();
        return tipo != null && clave(tipo.getEspecialidad()).equals(clave(empleado.getEspecialidad()));
    }

    private Estado estado(String nombre) {
        return estadoRepository.findFirstByNombreIgnoreCaseOrderByIdAsc(nombre).orElseGet(() -> {
            //la secuencia puede estar atrasada si hay estados cargados con ID explicito
            entityManager.createNativeQuery(
                    "SELECT setval(pg_get_serial_sequence('estados', 'id'), COALESCE((SELECT MAX(id) FROM estados), 0) + 1, false)")
                    .getSingleResult();
            Estado e = new Estado();
            e.setNombre(nombre);
            return estadoRepository.save(e);
        });
    }

    //Cierra el estado vigente y abre uno nuevo en el historial
    private void registrarEstado(Ticket ticket, Estado nuevo, Empleado responsable) {
        LocalDate hoy = LocalDate.now(ZONA);
        if (ticket.getHistorialEstados() == null) ticket.setHistorialEstados(new ArrayList<>());
        for (CambioEstado c : ticket.getHistorialEstados()) {
            if (c.getFechaHoraFin() == null) c.setFechaHoraFin(hoy);
        }
        CambioEstado cambio = new CambioEstado();
        cambio.setEstado(nuevo);
        cambio.setFechaHoraInicio(hoy);
        cambio.setEmpleado(responsable);
        ticket.getHistorialEstados().add(cambio);
    }

    private CambioEstado vigente(Ticket ticket) {
        List<CambioEstado> historial = ticket.getHistorialEstados();
        if (historial == null || historial.isEmpty()) return null;
        for (int i = historial.size() - 1; i >= 0; i--) {
            if (historial.get(i).getFechaHoraFin() == null) return historial.get(i);
        }
        return historial.get(historial.size() - 1);
    }

    private String codigoEstado(Ticket ticket) {
        CambioEstado v = vigente(ticket);
        String nombre = v == null || v.getEstado() == null ? "" : clave(v.getEstado().getNombre());
        if (nombre.contains("resuel")) return RESUELTO;
        if (nombre.contains("proceso")) return EN_PROCESO;
        return PENDIENTE;
    }

    private List<ReclamoResponse> aRespuestas(List<Ticket> tickets, boolean conCliente) {
        Map<Long, Cliente> clientes = new HashMap<>();
        if (conCliente && !tickets.isEmpty()) {
            List<Long> ids = tickets.stream().map(Ticket::getId).toList();
            for (Object[] fila : clienteRepository.findClientesPorTicketIds(ids)) {
                clientes.put((Long) fila[0], (Cliente) fila[1]);
            }
        }
        return tickets.stream().map(t -> aRespuesta(t, clientes.get(t.getId()))).toList();
    }

    private ReclamoResponse aRespuesta(Ticket t, Cliente cliente) {
        TipoIncidenteTecnico tipo = t.getIncidenteTecnico() == null ? null : t.getIncidenteTecnico().getTipoIncidente();
        CambioEstado vigente = vigente(t);
        Empleado tecnico = t.getEmpleado();

        List<ReclamoResponse.Seguimiento> historial = new ArrayList<>();
        if (t.getHistorialEstados() != null) {
            for (CambioEstado c : t.getHistorialEstados()) {
                Empleado resp = c.getEmpleado();
                historial.add(new ReclamoResponse.Seguimiento(
                        c.getEstado() == null ? null : c.getEstado().getNombre(),
                        c.getFechaHoraInicio(), c.getFechaHoraFin(),
                        resp == null ? null : resp.getNombre() + " " + resp.getApellido()));
            }
        }

        ReclamoResponse.ClienteResumen resumen = null;
        if (cliente != null) {
            resumen = new ReclamoResponse.ClienteResumen(
                    cliente.getId(), cliente.getNombre(), cliente.getApellido(), cliente.getTelefono(), domicilioTexto(cliente.getDomicilio()));
        }

        return new ReclamoResponse(
                t.getId(), t.getFechaCreacion(), t.getDescripcion(),
                tipo == null ? null : tipo.getId(),
                tipo == null ? t.getCategoria() : tipo.getNombre(),
                tipo == null ? null : tipo.getGravedad(),
                tipo == null ? null : tipo.getEspecialidad(),
                codigoEstado(t),
                vigente == null || vigente.getEstado() == null ? NOMBRE_PENDIENTE : vigente.getEstado().getNombre(),
                tecnico == null ? null : tecnico.getNombre() + " " + tecnico.getApellido(),
                t.getObservacionesResolucion(),
                historial,
                resumen);
    }

    private static String domicilioTexto(Domicilio d) {
        if (d == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(d.getCalle()).append(' ').append(d.getNumero());
        if (d.getPiso() != null && !d.getPiso().isBlank()) sb.append(", piso ").append(d.getPiso());
        if (d.getDepartamento() != null && !d.getDepartamento().isBlank()) sb.append(" dpto ").append(d.getDepartamento());
        if (d.getBarrio() != null) {
            sb.append(", B° ").append(d.getBarrio().getNombre());
            if (d.getBarrio().getLocalidad() != null) sb.append(" - ").append(d.getBarrio().getLocalidad().getNombre());
        }
        return sb.toString();
    }

    //Minusculas, sin acentos y sin espacios repetidos: "Técnico  de Redes" == "tecnico de redes"
    public static String clave(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
