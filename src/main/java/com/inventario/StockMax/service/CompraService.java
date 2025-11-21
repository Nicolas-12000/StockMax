package com.inventario.StockMax.service;

import com.inventario.StockMax.dto.CompraDTO;
import com.inventario.StockMax.exception.ResourceNotFoundException;
import com.inventario.StockMax.model.*;
import com.inventario.StockMax.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;

    @Transactional
    public CompraDTO registrarCompra(CompraDTO compraDTO) {
        Proveedor proveedor = proveedorRepository.findById(compraDTO.getProveedorId())
            .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        Compra compra = Compra.builder()
            .proveedor(proveedor)
            .total(BigDecimal.ZERO)
            .build();

        BigDecimal totalCompra = BigDecimal.ZERO;

        for (var detalleDTO : compraDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            BigDecimal subtotal = detalleDTO.getPrecioCompra()
                .multiply(BigDecimal.valueOf(detalleDTO.getCantidad()));

            DetalleCompra detalle = DetalleCompra.builder()
                .compra(compra)
                .producto(producto)
                .cantidad(detalleDTO.getCantidad())
                .precioCompra(detalleDTO.getPrecioCompra())
                .subtotal(subtotal)
                .build();

            compra.getDetalles().add(detalle);

            // Incrementar stock
            producto.setStock(producto.getStock() + detalleDTO.getCantidad());
            productoRepository.save(producto);

            totalCompra = totalCompra.add(subtotal);
        }

        compra.setTotal(totalCompra);
        compra = compraRepository.save(compra);

        return mapToDTO(compra);
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listarCompras() {
        return compraRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompraDTO obtenerCompra(Long id) {
        Compra compra = compraRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));
        return mapToDTO(compra);
    }

    private CompraDTO mapToDTO(Compra compra) {
        return CompraDTO.builder()
            .id(compra.getId())
            .proveedorId(compra.getProveedor().getId())
            .proveedorNombre(compra.getProveedor().getNombre())
            .fecha(compra.getFecha())
            .total(compra.getTotal())
            .build();
    }
}
