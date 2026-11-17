package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    //Reclamos que nadie tomo todavia (se filtran por area en el servicio)
    List<Ticket> findByEmpleadoIsNullOrderByIdAsc();

    //Reclamos tomados por un empleado
    List<Ticket> findByEmpleadoIdOrderByIdDesc(Long empleadoId);

    //Lee el ticket bloqueando la fila: dos tecnicos no pueden asignarse el mismo reclamo a la vez
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);
}
