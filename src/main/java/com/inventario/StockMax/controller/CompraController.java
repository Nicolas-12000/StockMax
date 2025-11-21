package com.inventario.StockMax.controller;

import com.inventario.StockMax.dto.CompraDTO;
import com.inventario.StockMax.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    public ResponseEntity<CompraDTO> registrarCompra(@Valid @RequestBody CompraDTO compraDTO) {
        CompraDTO registrada = compraService.registrarCompra(compraDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrada);
    }

    @GetMapping
    public ResponseEntity<List<CompraDTO>> listarCompras() {
        return ResponseEntity.ok(compraService.listarCompras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> obtenerCompra(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerCompra(id));
    }
}
