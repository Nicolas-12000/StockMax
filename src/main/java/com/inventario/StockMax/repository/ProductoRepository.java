package com.inventario.StockMax.repository;

import com.inventario.StockMax.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    List<Producto> findByCategoriaId(Long categoriaId);

    @Query("SELECT p FROM Producto p WHERE p.stock < 5")
    List<Producto> findProductosConStockBajo();

    @Query("SELECT p.categoria.nombre as categoria, p.nombre as producto, SUM(dv.cantidad) as totalVendido " +
           "FROM DetalleVenta dv JOIN dv.producto p JOIN dv.venta v " +
           "WHERE v.estado = 'CONFIRMADA' " +
           "GROUP BY p.categoria.id, p.id " +
           "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidosPorCategoria();

    @Query("SELECT pr.nombre as proveedor, c.nombre as categoria, p.nombre as producto, p.stock " +
           "FROM Producto p JOIN p.categoria c JOIN p.detallesCompra dc JOIN dc.compra co JOIN co.proveedor pr " +
           "GROUP BY pr.id, c.id, p.id " +
           "ORDER BY pr.nombre, c.nombre")
    List<Object[]> findStockPorProveedorYCategoria();
}
