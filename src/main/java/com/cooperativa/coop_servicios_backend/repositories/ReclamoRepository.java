package com.cooperativa.coop_servicios_backend.repositories;

import com.cooperativa.coop_servicios_backend.models.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {}