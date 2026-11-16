package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.EstadoSolicitud;
import com.cooperativa.coop_servicios_backend.models.SolicitudActualizacionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudActualizacionClienteRepository extends JpaRepository<SolicitudActualizacionCliente, Long> {

    boolean existsByClienteIdAndEstado(Long clienteId, EstadoSolicitud estado);

    Optional<SolicitudActualizacionCliente> findFirstByClienteIdOrderByFechaSolicitudDescIdDesc(Long clienteId);

    List<SolicitudActualizacionCliente> findAllByOrderByFechaSolicitudDesc();

    List<SolicitudActualizacionCliente> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);
}
