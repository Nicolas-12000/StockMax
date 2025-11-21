package com.inventario.StockMax.service;

import com.inventario.StockMax.dto.VentaDTO;
import com.inventario.StockMax.exception.InsufficientStockException;
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
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        Cliente cliente = clienteRepository.findById(ventaDTO.getClienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Validar stock antes de crear la venta
        for (var detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new InsufficientStockException(
                    String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                        producto.getNombre(), producto.getStock(), detalleDTO.getCantidad())
                );
            }
        }

        Venta venta = Venta.builder()
            .cliente(cliente)
            .estado(EstadoVenta.CREADA)
            .total(BigDecimal.ZERO)
            .build();

        BigDecimal totalVenta = BigDecimal.ZERO;

        for (var detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            BigDecimal subtotal = detalleDTO.getPrecioVenta()
                .multiply(BigDecimal.valueOf(detalleDTO.getCantidad()));

            DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .producto(producto)
                .cantidad(detalleDTO.getCantidad())
                .precioVenta(detalleDTO.getPrecioVenta())
                .subtotal(subtotal)
                .build();

            venta.getDetalles().add(detalle);
            totalVenta = totalVenta.add(subtotal);
        }

        venta.setTotal(totalVenta);
        venta = ventaRepository.save(venta);

        return mapToDTO(venta);
    }

    @Transactional
    public VentaDTO confirmarVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        if (venta.getEstado() != EstadoVenta.CREADA) {
            throw new IllegalStateException("Solo se pueden confirmar ventas en estado CREADA");
        }

        // Decrementar stock
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        venta.setEstado(EstadoVenta.CONFIRMADA);
        venta = ventaRepository.save(venta);

        return mapToDTO(venta);
    }

    @Transactional
    public VentaDTO cancelarVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.CONFIRMADA) {
            // Revertir stock si ya estaba confirmada
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        venta = ventaRepository.save(venta);

        return mapToDTO(venta);
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarVentas() {
        return ventaRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));
        return mapToDTO(venta);
    }

    private VentaDTO mapToDTO(Venta venta) {
        return VentaDTO.builder()
            .id(venta.getId())
            .clienteId(venta.getCliente().getId())
            .clienteNombre(venta.getCliente().getNombre())
            .fecha(venta.getFecha())
            .total(venta.getTotal())
            .estado(venta.getEstado())
            .build();
    }
}
