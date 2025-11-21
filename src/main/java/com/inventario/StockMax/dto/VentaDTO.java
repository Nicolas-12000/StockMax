package com.inventario.StockMax.dto;

import com.inventario.StockMax.model.EstadoVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {
    private Long id;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    private String clienteNombre;

    private LocalDateTime fecha;

    private BigDecimal total;

    private EstadoVenta estado;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<DetalleVentaDTO> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleVentaDTO {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        private String productoNombre;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        private BigDecimal precioVenta;

        private BigDecimal subtotal;
    }
}
 
