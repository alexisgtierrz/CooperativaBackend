package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Busca el legajo mas alto (comparacion de texto)
    @Query("SELECT MAX(e.legajo) FROM Empleado e")
    String findMaxLegajo();

    // Legajo mas alto como numero (0 si no hay empleados)
    @Query(value = "SELECT COALESCE(MAX(CAST(NULLIF(regexp_replace(legajo, '\\D', '', 'g'), '') AS INTEGER)), 0) FROM empleados",
            nativeQuery = true)
    Integer findMaxLegajoNumerico();

    boolean existsByDni(String dni);

    boolean existsByUsuarioId(Long usuarioId);
}
