package com.inventario.StockMax.controller;

import com.inventario.StockMax.dto.VentaDTO;
import com.inventario.StockMax.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaDTO> crearVenta(@Valid @RequestBody VentaDTO ventaDTO) {
        VentaDTO creada = ventaService.crearVenta(ventaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<VentaDTO> confirmarVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.confirmarVenta(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<VentaDTO> cancelarVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.cancelarVenta(id));
    }

    @GetMapping
    public ResponseEntity<List<VentaDTO>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerVenta(id));
    }
}
