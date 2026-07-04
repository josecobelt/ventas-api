package com.pruebatecnica.ventas.controller;

import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import com.pruebatecnica.ventas.dto.articulo.ArticuloResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import com.pruebatecnica.ventas.service.ArticuloService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/articulos")
@Tag(name = "Artículos", description = "Alta, baja, modificación y consulta de artículos")
public class ArticuloController {

    private final ArticuloService articuloService;

    public ArticuloController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    @PostMapping
    public ResponseEntity<ArticuloResponseDTO> crear(@Valid @RequestBody ArticuloRequestDTO dto) {
        ArticuloResponseDTO creado = articuloService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ArticuloResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return ResponseEntity.ok(articuloService.listar(pageable, soloActivos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(articuloService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticuloResponseDTO> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody ArticuloRequestDTO dto) {
        return ResponseEntity.ok(articuloService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean permanente) {
        articuloService.eliminar(id, permanente);
        return ResponseEntity.noContent().build();
    }
}
