package com.ebv14.backend.repository;

import com.ebv14.backend.model.AlertaPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertaPresupuestoRepository extends JpaRepository<AlertaPresupuesto, Long> {

    // Obtener alertas del usuario en el mes actual
    @Query("SELECT a FROM AlertaPresupuesto a WHERE a.usuario.id = :usuarioId AND a.mesAno = :mesAno ORDER BY a.fechaCreacion DESC")
    List<AlertaPresupuesto> findByUsuarioAndMes(
            @Param("usuarioId") Long usuarioId,
            @Param("mesAno") String mesAno
    );

    // Obtener la última alerta del usuario
    @Query("SELECT a FROM AlertaPresupuesto a WHERE a.usuario.id = :usuarioId ORDER BY a.fechaCreacion DESC LIMIT 1")
    Optional<AlertaPresupuesto> findLastAlert(@Param("usuarioId") Long usuarioId);

    // Obtener alertas críticas del usuario
    List<AlertaPresupuesto> findByUsuarioIdAndTipoAlerta(Long usuarioId, AlertaPresupuesto.TipoAlerta tipoAlerta);
}