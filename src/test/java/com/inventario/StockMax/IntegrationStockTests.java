package com.inventario.StockMax;

import com.inventario.StockMax.dto.CompraDTO;
import com.inventario.StockMax.dto.VentaDTO;
import com.inventario.StockMax.exception.InsufficientStockException;
import com.inventario.StockMax.model.Categoria;
import com.inventario.StockMax.model.Cliente;
import com.inventario.StockMax.model.Producto;
import com.inventario.StockMax.model.Proveedor;
import com.inventario.StockMax.repository.CategoriaRepository;
import com.inventario.StockMax.repository.ClienteRepository;
import com.inventario.StockMax.repository.ProductoRepository;
import com.inventario.StockMax.repository.ProveedorRepository;
import com.inventario.StockMax.service.CompraService;
import com.inventario.StockMax.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class IntegrationStockTests {

    @Autowired
    private CompraService compraService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Producto producto;
    private Proveedor proveedor;
    private Cliente cliente;

    @BeforeEach
    public void setup() {
        categoriaRepository.deleteAll();
        proveedorRepository.deleteAll();
        productoRepository.deleteAll();
        clienteRepository.deleteAll();

        Categoria cat = Categoria.builder().nombre("TestCat").descripcion("desc").build();
        cat = categoriaRepository.save(cat);

        proveedor = Proveedor.builder().nombre("ProvTest").ruc("RUC1").build();
        proveedor = proveedorRepository.save(proveedor);

        producto = Producto.builder()
                .codigo("X100")
                .nombre("Prod Test")
                .stock(5)
                .precioVenta(new BigDecimal("10.00"))
                .categoria(cat)
                .build();
        producto = productoRepository.save(producto);

        cliente = Cliente.builder().nombre("Client Test").documento("DNI1").build();
        cliente = clienteRepository.save(cliente);
    }

    @Test
    @Transactional
    public void compraIncrementaStock() {
        int inicial = producto.getStock();

        CompraDTO.DetalleCompraDTO detalle = CompraDTO.DetalleCompraDTO.builder()
                .productoId(producto.getId())
                .cantidad(3)
                .precioCompra(new BigDecimal("2.50"))
                .build();

        CompraDTO compra = CompraDTO.builder()
                .proveedorId(proveedor.getId())
                .detalles(List.of(detalle))
                .build();

        CompraDTO result = compraService.registrarCompra(compra);
        Producto p = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(p.getStock()).isEqualTo(inicial + 3);
        assertThat(result.getTotal()).isNotNull();
    }

    @Test
    @Transactional
    public void ventaCreacionBloqueadaPorStockInsuficiente() {
        VentaDTO.DetalleVentaDTO detalle = VentaDTO.DetalleVentaDTO.builder()
                .productoId(producto.getId())
                .cantidad(10)
                .precioVenta(new BigDecimal("12.00"))
                .build();

        VentaDTO venta = VentaDTO.builder()
                .clienteId(cliente.getId())
                .detalles(List.of(detalle))
                .build();

        assertThrows(InsufficientStockException.class, () -> ventaService.crearVenta(venta));
    }

    @Test
    @Transactional
    public void confirmarVentaDecrementaYCancelarReversaStock() {
        // Asegurar stock suficiente
        Producto pBefore = productoRepository.findById(producto.getId()).orElseThrow();
        pBefore.setStock(10);
        productoRepository.save(pBefore);

        VentaDTO.DetalleVentaDTO detalle = VentaDTO.DetalleVentaDTO.builder()
                .productoId(producto.getId())
                .cantidad(4)
                .precioVenta(new BigDecimal("12.00"))
                .build();

        VentaDTO venta = VentaDTO.builder()
                .clienteId(cliente.getId())
                .detalles(List.of(detalle))
                .build();

        VentaDTO created = ventaService.crearVenta(venta);

        // after create, stock must remain unchanged
        Producto pAfterCreate = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(pAfterCreate.getStock()).isEqualTo(10);

        // confirm
        VentaDTO confirmed = ventaService.confirmarVenta(created.getId());
        Producto pAfterConfirm = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(pAfterConfirm.getStock()).isEqualTo(6);
        assertThat(confirmed.getEstado()).isNotNull();

        // cancel should revert stock because it was CONFIRMADA
        VentaDTO canceled = ventaService.cancelarVenta(created.getId());
        Producto pAfterCancel = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(pAfterCancel.getStock()).isEqualTo(10);
        assertThat(canceled.getEstado()).isNotNull();
    }
}
