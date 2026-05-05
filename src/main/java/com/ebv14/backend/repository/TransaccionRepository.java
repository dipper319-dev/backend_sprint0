package com.ebv14.backend.repository;

import com.ebv14.backend.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsuarioIdOrderByFechaTransaccionDesc(Long usuarioId);

    List<Transaccion> findByUsuarioIdAndTipo(Long usuarioId, Transaccion.TipoTransaccion tipo);

    List<Transaccion> findByUsuarioIdAndTipoOrderByFechaTransaccionDesc(Long usuarioId, Transaccion.TipoTransaccion tipo);

    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.usuario.id = :usuarioId AND t.tipo = :tipo")
    BigDecimal sumByUsuarioIdAndTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") Transaccion.TipoTransaccion tipo);

    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.usuario.id = :usuarioId AND t.tipo = 'INGRESO'")
    BigDecimal sumIngresosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t WHERE t.usuario.id = :usuarioId AND t.tipo = 'GASTO'")
    BigDecimal sumGastosByUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Transaccion> findByUsuarioIdAndCategoriaIdAndTipo(Long usuarioId, Long categoriaId, Transaccion.TipoTransaccion tipo);

    @Query("SELECT COUNT(t) FROM Transaccion t WHERE t.usuario.id = :usuarioId AND t.tipo = 'GASTO'")
    long countGastosByUsuario(@Param("usuarioId") Long usuarioId);
}