package com.cooperativa.coop_servicios_backend.services;

import com.cooperativa.coop_servicios_backend.DTOs.CobranzaResponse;
import com.cooperativa.coop_servicios_backend.DTOs.FacturaResponse;
import com.cooperativa.coop_servicios_backend.DTOs.PagoRequests;
import com.cooperativa.coop_servicios_backend.models.Cliente;
import com.cooperativa.coop_servicios_backend.models.DetalleFactura;
import com.cooperativa.coop_servicios_backend.models.EstadoFactura;
import com.cooperativa.coop_servicios_backend.models.Factura;
import com.cooperativa.coop_servicios_backend.models.MedioPago;
import com.cooperativa.coop_servicios_backend.models.Pago;
import com.cooperativa.coop_servicios_backend.models.Suscripcion;
import com.cooperativa.coop_servicios_backend.repositories.ClienteRepository;
import com.cooperativa.coop_servicios_backend.repositories.FacturaRepository;
import com.cooperativa.coop_servicios_backend.repositories.MedioPagoRepository;
import com.cooperativa.coop_servicios_backend.repositories.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Facturas del asociado y cobro con Mercado Pago
@Service
public class FacturaPagoService {

    public static final String MEDIO_PAGO_MP = "Mercado Pago";

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private MedioPagoRepository medioPagoRepository;
    @Autowired private MercadoPagoClient mercadoPago;

    //URL del frontend a la que vuelve el usuario desde Mercado Pago
    @Value("${mp.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    //URL publica del webhook
    @Value("${mp.notification-url:}")
    private String notificationUrl;

    //auto_return=approved: Mercado Pago vuelve solo al sitio tras un pago aprobado
    @Value("${mp.auto-return:true}")
    private boolean autoReturn;

    // ------------------------------------------------------------------ consulta

    //Facturas del usuario autenticado; con estado "PENDIENTE" o "PAGADA" se filtra, sin estado trae todas
    @Transactional
    public List<FacturaResponse> misFacturas(String emailUsuario, String estado) {
        Cliente cliente = clientePorEmail(emailUsuario);
        emitirFacturasDelCiclo(cliente.getId());
        reconciliarPagosPendientes(cliente.getId());
        EstadoFactura filtro = parsearEstado(estado);
        List<FacturaResponse> resultado = new ArrayList<>();
        for (Factura f : facturaRepository.findByClienteIdOrderByFechaVencimientoAsc(cliente.getId())) {
            if (filtro == null || f.getEstado() == filtro) resultado.add(aRespuesta(f));
        }
        return resultado;
    }

    //emision de facturas

    //Genera la factura de cada ciclo de servicio: las suscripciones activas del cliente que vencen el mismo dia se agrupan en una sola factura (un detalle por servicio, al precio de su tarifa). Se emite cuando el vencimiento esta a 31 dias o menos, asi el asociado puede pagar apenas el ciclo existe
    private void emitirFacturasDelCiclo(Long clienteId) {
        Cliente cliente = clienteRepository.findByIdForUpdate(clienteId).orElseThrow();
        if (cliente.getSuscripciones() == null || cliente.getSuscripciones().isEmpty()) return;

        //Pares "suscripcionId|vencimiento" que ya tienen factura
        Set<String> facturados = new HashSet<>();
        for (Factura f : facturaRepository.findByClienteIdOrderByFechaVencimientoAsc(clienteId)) {
            if (f.getDetalles() == null) continue;
            for (DetalleFactura d : f.getDetalles()) {
                if (d.getSuscripcion() != null) facturados.add(d.getSuscripcion().getId() + "|" + f.getFechaVencimiento());
            }
        }

        LocalDate hoy = LocalDate.now();
        Map<LocalDate, List<Suscripcion>> porVencimiento = new TreeMap<>();
        for (Suscripcion s : cliente.getSuscripciones()) {
            if (s.getFechaBaja() != null || s.getFechaHasta() == null) continue;
            if (s.getServicio() == null || s.getServicio().getTarifa() == null) continue;
            if (s.getFechaHasta().isAfter(hoy.plusDays(31))) continue;
            if (facturados.contains(s.getId() + "|" + s.getFechaHasta())) continue;
            porVencimiento.computeIfAbsent(s.getFechaHasta(), k -> new ArrayList<>()).add(s);
        }

        for (Map.Entry<LocalDate, List<Suscripcion>> e : porVencimiento.entrySet()) {
            Factura factura = new Factura();
            factura.setCliente(cliente);
            factura.setFechaVencimiento(e.getKey());
            LocalDate inicioCiclo = e.getKey().minusMonths(1);
            factura.setFechaEmision(inicioCiclo.isAfter(hoy) ? hoy : inicioCiclo);
            factura.setEstado(EstadoFactura.PENDIENTE);

            List<DetalleFactura> detalles = new ArrayList<>();
            double total = 0;
            for (Suscripcion s : e.getValue()) {
                double precio = s.getServicio().getTarifa().getMonto();
                DetalleFactura d = new DetalleFactura();
                d.setSuscripcion(s);
                d.setCantidad(1);
                d.setPrecioUnitario(precio);
                d.setSubTotal(precio);
                detalles.add(d);
                total += precio;
            }
            factura.setDetalles(detalles);
            factura.setMontoTotal(total);
            facturaRepository.save(factura);
        }
    }

    // ------------------------------------------------------------------ cobranzas (administrador)

    //Situacion de cobranza de todos los clientes: servicios activos, facturas, deuda y atraso. Antes de armarla. Ordenada con los mayores deudores primero.
    @Transactional
    public List<CobranzaResponse> cobranzas() {
        LocalDate hoy = LocalDate.now();
        List<CobranzaResponse> resultado = new ArrayList<>();
        for (Cliente c : clienteRepository.findAll()) {
            emitirFacturasDelCiclo(c.getId());

            List<CobranzaResponse.ServicioContratado> servicios = new ArrayList<>();
            if (c.getSuscripciones() != null) {
                for (Suscripcion s : c.getSuscripciones()) {
                    if (s.getFechaBaja() != null || s.getServicio() == null) continue;
                    servicios.add(new CobranzaResponse.ServicioContratado(
                            s.getServicio().getNombre(), s.getFechaHasta() == null ? null : s.getFechaHasta().toString()));
                }
            }

            List<FacturaResponse> facturas = new ArrayList<>();
            int pendientes = 0, vencidas = 0;
            double deuda = 0, deudaVencida = 0;
            long maxAtraso = 0;
            LocalDate proximo = null;
            for (Factura f : facturaRepository.findByClienteIdOrderByFechaVencimientoAsc(c.getId())) {
                facturas.add(aRespuesta(f));
                if (f.getEstado() != EstadoFactura.PENDIENTE) continue;
                pendientes++;
                deuda += f.getMontoTotal();
                if (f.getFechaVencimiento().isBefore(hoy)) {
                    vencidas++;
                    deudaVencida += f.getMontoTotal();
                    maxAtraso = Math.max(maxAtraso, java.time.temporal.ChronoUnit.DAYS.between(f.getFechaVencimiento(), hoy));
                } else if (proximo == null || f.getFechaVencimiento().isBefore(proximo)) {
                    proximo = f.getFechaVencimiento();
                }
            }
            java.util.Collections.reverse(facturas); // las mas recientes primero

            String nombre = ((c.getApellido() == null ? "" : c.getApellido()) + ", " + (c.getNombre() == null ? "" : c.getNombre())).trim();
            resultado.add(new CobranzaResponse(c.getId(), nombre, c.getDni(), c.getEmail(), c.getTelefono(),
                    !Boolean.FALSE.equals(c.getActivo()), servicios, pendientes, vencidas, deuda, deudaVencida,
                    maxAtraso, proximo == null ? null : proximo.toString(), facturas));
        }
        resultado.sort(java.util.Comparator
                .comparingDouble(CobranzaResponse::deudaVencida).reversed()
                .thenComparing(java.util.Comparator.comparingDouble(CobranzaResponse::deudaTotal).reversed())
                .thenComparing(CobranzaResponse::nombre, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    //conciliacion automatica

    //Si el usuario pago en Mercado Pago pero no volvio al sitio (y el webhook no pudo llegar, p. ej. en localhost), la factura quedaria PENDIENTE. Por eso, al listar, se consulta a Mercado Pago si cada factura pendiente ya tiene un pago aprobado (por su external_reference) y, de ser asi, se acredita. Nunca rompe el listado: ante cualquier falla de Mercado Pago la factura simplemente sigue pendiente
    private void reconciliarPagosPendientes(Long clienteId) {
        for (Factura f : facturaRepository.findByClienteIdOrderByFechaVencimientoAsc(clienteId)) {
            if (f.getEstado() != EstadoFactura.PENDIENTE) continue;
            try {
                for (Map<String, Object> pago : mercadoPago.buscarPagosAprobados(String.valueOf(f.getId()))) {
                    String paymentId = texto(pago.get("id"));
                    Object monto = pago.get("transaction_amount");
                    boolean montoOk = !(monto instanceof Number n) || Math.abs(n.doubleValue() - f.getMontoTotal()) <= 0.01;
                    if (!"approved".equals(texto(pago.get("status"))) || paymentId.isBlank() || !montoOk) continue;
                    Factura bloqueada = facturaRepository.findByIdForUpdate(f.getId()).orElse(null);
                    if (bloqueada != null && bloqueada.getEstado() == EstadoFactura.PENDIENTE) acreditar(bloqueada, paymentId);
                    break;
                }
            } catch (RuntimeException e) {
                // Mercado Pago no configurado o sin respuesta: se deja la factura como esta
            }
        }
    }

    //preferencia de pago

    @Transactional(readOnly = true)
    public PagoRequests.Preferencia crearPreferencia(String emailUsuario, Long facturaId) {
        if (facturaId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la factura a pagar.");
        Cliente cliente = clientePorEmail(emailUsuario);
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La factura no existe."));
        verificarPropietario(factura, cliente);
        if (factura.getEstado() != EstadoFactura.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta factura ya fue pagada.");
        }

        String base = frontendUrl.replaceAll("/+$", "");
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(factura.getId()));
        item.put("title", "Factura #" + factura.getId() + " - Cooperativa de Servicios");
        item.put("description", String.join(", ", serviciosDe(factura)));
        item.put("quantity", 1);
        item.put("unit_price", factura.getMontoTotal());
        item.put("currency_id", "ARS");

        Map<String, Object> backUrls = new LinkedHashMap<>();
        backUrls.put("success", base + "/pagar-factura/resultado/success");
        backUrls.put("failure", base + "/pagar-factura/resultado/failure");
        backUrls.put("pending", base + "/pagar-factura/resultado/pending");

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("items", List.of(item));
        cuerpo.put("external_reference", String.valueOf(factura.getId()));
        cuerpo.put("back_urls", backUrls);
        if (autoReturn) cuerpo.put("auto_return", "approved");
        if (notificationUrl != null && !notificationUrl.isBlank()) cuerpo.put("notification_url", notificationUrl.trim());

        Map<String, Object> respuesta;
        try {
            respuesta = mercadoPago.crearPreferencia(cuerpo);
        } catch (ResponseStatusException e) {
            if (!cuerpo.containsKey("auto_return") || e.getStatusCode() != HttpStatus.BAD_GATEWAY) throw e;
            cuerpo.remove("auto_return");
            respuesta = mercadoPago.crearPreferencia(cuerpo);
        }
        return new PagoRequests.Preferencia(
                texto(respuesta.get("id")), texto(respuesta.get("init_point")), texto(respuesta.get("sandbox_init_point")));
    }

    //confirmacion

    //Confirma un pago consultandolo directo a Mercado Pago. Sirve para el webhook (emailUsuario = null) y para el retorno del usuario (verifica que la factura sea suya)
    @Transactional
    public PagoRequests.Resultado confirmarPago(String paymentId, String emailUsuario) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el identificador del pago.");
        }
        Map<String, Object> pago = mercadoPago.obtenerPago(paymentId.trim());
        String estadoMp = texto(pago.get("status"));
        Long facturaId = parsearId(texto(pago.get("external_reference")));
        if (facturaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago no corresponde a ninguna factura.");
        }

        Factura factura = facturaRepository.findByIdForUpdate(facturaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La factura del pago no existe."));
        if (emailUsuario != null) verificarPropietario(factura, clientePorEmail(emailUsuario));

        if (!"approved".equals(estadoMp)) {
            String mensaje = switch (estadoMp) {
                case "pending", "in_process", "authorized" -> "Tu pago está pendiente de acreditación. Cuando se confirme, la factura figurará como pagada.";
                case "rejected" -> "El pago fue rechazado. Podés intentarlo de nuevo con otro medio de pago.";
                case "cancelled" -> "El pago fue cancelado.";
                default -> "El pago no fue aprobado (estado: " + estadoMp + ").";
            };
            return new PagoRequests.Resultado(false, estadoMp, facturaId, mensaje);
        }

        if (factura.getEstado() == EstadoFactura.PAGADA) {
            return new PagoRequests.Resultado(true, estadoMp, facturaId, "La factura ya figura como pagada.");
        }

        Object monto = pago.get("transaction_amount");
        if (monto instanceof Number n && Math.abs(n.doubleValue() - factura.getMontoTotal()) > 0.01) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El importe pagado no coincide con el de la factura.");
        }

        acreditar(factura, paymentId.trim());
        return new PagoRequests.Resultado(true, estadoMp, facturaId, "¡Pago acreditado! Tu factura quedó pagada.");
    }

    //Marca la factura como PAGADA, renueva las suscripciones cubiertas y registra el pago
    private void acreditar(Factura factura, String paymentId) {
        factura.setEstado(EstadoFactura.PAGADA);
        factura.setFechaPago(LocalDateTime.now());
        factura.setIdOperacion(paymentId);
        facturaRepository.save(factura);

        //El pago renueva el servicio: cada suscripcion cubierta por la factura vence un mes despues
        if (factura.getDetalles() != null) {
            for (DetalleFactura d : factura.getDetalles()) {
                Suscripcion sus = d.getSuscripcion();
                if (sus != null && factura.getFechaVencimiento().equals(sus.getFechaHasta())) {
                    sus.setFechaHasta(sus.getFechaHasta().plusMonths(1));
                }
            }
        }

        if (!pagoRepository.existsByNumeroComprobante(paymentId)) {
            Pago registro = new Pago();
            registro.setFactura(factura);
            registro.setMedioPago(medioPagoMercadoPago());
            registro.setMontoPagado(factura.getMontoTotal());
            registro.setFechaPago(factura.getFechaPago());
            registro.setNumeroComprobante(paymentId);
            pagoRepository.save(registro);
        }
    }

    //auxiliares

    private MedioPago medioPagoMercadoPago() {
        return medioPagoRepository.findAll().stream()
                .filter(m -> MEDIO_PAGO_MP.equalsIgnoreCase(m.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    MedioPago nuevo = new MedioPago();
                    nuevo.setNombre(MEDIO_PAGO_MP);
                    nuevo.setEsActivo(true);
                    return medioPagoRepository.save(nuevo);
                });
    }

    private Cliente clientePorEmail(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tu usuario no tiene una ficha de cliente."));
    }

    private void verificarPropietario(Factura factura, Cliente cliente) {
        if (factura.getCliente() == null || !factura.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta factura no te pertenece.");
        }
    }

    private FacturaResponse aRespuesta(Factura f) {
        boolean pendiente = f.getEstado() == EstadoFactura.PENDIENTE;
        LocalDate venc = f.getFechaVencimiento();
        return new FacturaResponse(
                f.getId(),
                String.format("%d-%02d", f.getFechaEmision().getYear(), f.getFechaEmision().getMonthValue()),
                f.getFechaEmision().toString(),
                venc.toString(),
                f.getMontoTotal(),
                f.getEstado().name(),
                pendiente && venc.isBefore(LocalDate.now()),
                serviciosDe(f),
                f.getFechaPago() == null ? null : f.getFechaPago().toString(),
                f.getIdOperacion());
    }

    private List<String> serviciosDe(Factura f) {
        Set<String> nombres = new LinkedHashSet<>();
        if (f.getDetalles() != null) {
            for (DetalleFactura d : f.getDetalles()) {
                if (d.getSuscripcion() != null && d.getSuscripcion().getServicio() != null) {
                    nombres.add(d.getSuscripcion().getServicio().getNombre());
                }
            }
        }
        return new ArrayList<>(nombres);
    }

    private static EstadoFactura parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) return null;
        String e = estado.trim().toUpperCase();
        if (e.equals("IMPAGA")) e = "PENDIENTE";
        try {
            return EstadoFactura.valueOf(e);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de factura inválido: " + estado);
        }
    }

    private static Long parsearId(String texto) {
        try {
            return texto == null || texto.isBlank() ? null : Long.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String texto(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
