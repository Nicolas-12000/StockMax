package com.inventario.StockMax;

import com.inventario.StockMax.dto.VentaDTO;
import com.inventario.StockMax.dto.VentaDTO.DetalleVentaDTO;
import com.inventario.StockMax.model.Categoria;
import com.inventario.StockMax.model.Cliente;
import com.inventario.StockMax.model.Producto;
import com.inventario.StockMax.model.EstadoVenta;
import com.inventario.StockMax.repository.CategoriaRepository;
import com.inventario.StockMax.repository.ClienteRepository;
import com.inventario.StockMax.repository.ProductoRepository;
import com.inventario.StockMax.repository.VentaRepository;
import com.inventario.StockMax.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrentStockTests {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Test
    public void concurrentConfirmationsTriggerOptimisticLocking() throws Exception {
        // Arrange: create category, product with stock 1, and a client
        Categoria cat = categoriaRepository.save(Categoria.builder().nombre("TestCat").descripcion("desc").build());
        Producto prod = Producto.builder()
                .codigo("C100")
                .nombre("Concurrent Product")
                .descripcion("desc")
                .precioVenta(new BigDecimal("10.00"))
                .stock(1)
                .categoria(cat)
                .build();
        prod = productoRepository.save(prod);

        Cliente cliente = clienteRepository.save(Cliente.builder().nombre("Cliente Test").documento("DNI-CONC").build());

        // Create two ventas each requesting 1 unit
        DetalleVentaDTO detalle = DetalleVentaDTO.builder()
                .productoId(prod.getId())
                .cantidad(1)
                .precioVenta(prod.getPrecioVenta())
                .build();

        VentaDTO v1Req = VentaDTO.builder().clienteId(cliente.getId()).detalles(List.of(detalle)).build();
        VentaDTO v2Req = VentaDTO.builder().clienteId(cliente.getId()).detalles(List.of(detalle)).build();

        final VentaDTO v1 = ventaService.crearVenta(v1Req);
        final VentaDTO v2 = ventaService.crearVenta(v2Req);

        // Act: try to confirm both ventas concurrently
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicReference<Exception> firstException = new AtomicReference<>();

        Runnable confirmTask1 = () -> {
            try {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                ventaService.confirmarVenta(v1.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                firstException.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        };

        Runnable confirmTask2 = () -> {
            try {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                ventaService.confirmarVenta(v2.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                firstException.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        };

        Thread t1 = new Thread(confirmTask1, "confirmer-1");
        Thread t2 = new Thread(confirmTask2, "confirmer-2");
        t1.start();
        t2.start();

        // Wait both ready then start
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();

        // Wait for completion
        done.await(10, TimeUnit.SECONDS);

        // Assert: only one confirmation should succeed
        assertThat(successCount.get()).isEqualTo(1);

        // Reload product and ventas
        Producto prodAfter = productoRepository.findById(prod.getId()).orElseThrow();
        assertThat(prodAfter.getStock()).isEqualTo(0);

        var venta1 = ventaRepository.findById(v1.getId()).orElseThrow();
        var venta2 = ventaRepository.findById(v2.getId()).orElseThrow();

        int confirmed = 0;
        if (venta1.getEstado() == EstadoVenta.CONFIRMADA) confirmed++;
        if (venta2.getEstado() == EstadoVenta.CONFIRMADA) confirmed++;

        assertThat(confirmed).isEqualTo(1);
    }
}
