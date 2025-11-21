package com.inventario.StockMax.controller;

import com.inventario.StockMax.repository.ProductoRepository;
import com.inventario.StockMax.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<Map<String, Object>>> productosMasVendidos() {
        List<Object[]> resultados = productoRepository.findProductosMasVendidosPorCategoria();

        List<Map<String, Object>> reportes = new ArrayList<>();
        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoria", row[0]);
            item.put("producto", row[1]);
            item.put("totalVendido", row[2]);
            reportes.add(item);
        }

        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/stock-por-proveedor-categoria")
    public ResponseEntity<List<Map<String, Object>>> stockPorProveedorCategoria() {
        List<Object[]> resultados = productoRepository.findStockPorProveedorYCategoria();

        List<Map<String, Object>> reportes = new ArrayList<>();
        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("proveedor", row[0]);
            item.put("categoria", row[1]);
            item.put("producto", row[2]);
            item.put("stock", row[3]);
            reportes.add(item);
        }

        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/ventas-mensuales-clientes")
    public ResponseEntity<List<Map<String, Object>>> ventasMensualesPorCliente() {
        List<Object[]> resultados = ventaRepository.findVentasMensualesPorCliente();

        List<Map<String, Object>> reportes = new ArrayList<>();
        for (Object[] row : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("cliente", row[0]);
            item.put("anio", row[1]);
            item.put("mes", row[2]);
            item.put("totalVentas", row[3]);
            item.put("cantidadVentas", row[4]);
            reportes.add(item);
        }

        return ResponseEntity.ok(reportes);
    }
}
