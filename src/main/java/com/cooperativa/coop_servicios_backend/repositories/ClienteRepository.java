package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.Cliente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByUsuarioEmail(String email);

    //Bloquea la fila del cliente mientras se emiten sus facturas (evita facturas duplicadas por pedidos simultaneos)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> findByIdForUpdate(@Param("id") Long id);

    //Pares [ticketId, Cliente] para saber a quien pertenece cada ticket
    @Query("select t.id, c from Cliente c join c.tickets t where t.id in :ids")
    List<Object[]> findClientesPorTicketIds(@Param("ids") Collection<Long> ids);
}
