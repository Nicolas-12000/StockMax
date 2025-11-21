package com.inventario.StockMax.repository;

import com.inventario.StockMax.model.Venta;
import com.inventario.StockMax.model.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByClienteId(Long clienteId);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT c.nombre as cliente, YEAR(v.fecha) as anio, MONTH(v.fecha) as mes, SUM(v.total) as totalVentas, COUNT(v.id) as cantidadVentas " +
           "FROM Venta v JOIN v.cliente c WHERE v.estado = 'CONFIRMADA' " +
           "GROUP BY c.id, YEAR(v.fecha), MONTH(v.fecha) " +
           "ORDER BY anio DESC, mes DESC, totalVentas DESC")
    List<Object[]> findVentasMensualesPorCliente();
}
