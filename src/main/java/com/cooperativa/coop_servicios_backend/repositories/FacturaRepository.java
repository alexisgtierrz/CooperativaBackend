package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.Factura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByClienteIdOrderByFechaVencimientoAsc(Long clienteId);

    //Bloquea la fila: evita que el webhook y el retorno del pago acrediten la misma factura dos veces
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.id = :id")
    Optional<Factura> findByIdForUpdate(@Param("id") Long id);
}
