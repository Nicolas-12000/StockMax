package com.inventario.StockMax.service;

import com.inventario.StockMax.dto.ProductoDTO;
import com.inventario.StockMax.exception.ResourceNotFoundException;
import com.inventario.StockMax.model.*;
import com.inventario.StockMax.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Producto producto = Producto.builder()
            .codigo(productoDTO.getCodigo())
            .nombre(productoDTO.getNombre())
            .descripcion(productoDTO.getDescripcion())
            .stock(0)
            .precioVenta(productoDTO.getPrecioVenta())
            .categoria(categoria)
            .build();

        producto = productoRepository.save(producto);
        return mapToDTO(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerProductosStockBajo() {
        return productoRepository.findProductosConStockBajo().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    private ProductoDTO mapToDTO(Producto producto) {
        return ProductoDTO.builder()
            .id(producto.getId())
            .codigo(producto.getCodigo())
            .nombre(producto.getNombre())
            .descripcion(producto.getDescripcion())
            .stock(producto.getStock())
            .precioVenta(producto.getPrecioVenta())
            .categoriaId(producto.getCategoria().getId())
            .categoriaNombre(producto.getCategoria().getNombre())
            .build();
    }
}
