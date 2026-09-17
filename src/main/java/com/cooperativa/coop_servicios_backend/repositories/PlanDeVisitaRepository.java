package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.PlanDeVisita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanDeVisitaRepository extends JpaRepository<PlanDeVisita, Long> {
}