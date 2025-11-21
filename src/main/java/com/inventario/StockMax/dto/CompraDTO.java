package com.inventario.StockMax.dto;

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
public class CompraDTO {
    private Long id;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    private String proveedorNombre;

    private LocalDateTime fecha;

    private BigDecimal total;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<DetalleCompraDTO> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleCompraDTO {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        private String productoNombre;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        private BigDecimal precioCompra;

        private BigDecimal subtotal;
    }
}
 
