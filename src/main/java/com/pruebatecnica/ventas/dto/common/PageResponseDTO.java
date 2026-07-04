package com.pruebatecnica.ventas.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envoltorio simple para no exponer directamente el tipo {@link Page} de
 * Spring Data en las respuestas de la API.
 */
public class PageResponseDTO<T> {

    private List<T> contenido;
    private int paginaActual;
    private int tamanioPagina;
    private long totalElementos;
    private int totalPaginas;
    private boolean ultima;

    public PageResponseDTO() {
    }

    public PageResponseDTO(Page<T> page) {
        this.contenido = page.getContent();
        this.paginaActual = page.getNumber();
        this.tamanioPagina = page.getSize();
        this.totalElementos = page.getTotalElements();
        this.totalPaginas = page.getTotalPages();
        this.ultima = page.isLast();
    }

    public List<T> getContenido() {
        return contenido;
    }

    public void setContenido(List<T> contenido) {
        this.contenido = contenido;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    public int getTamanioPagina() {
        return tamanioPagina;
    }

    public void setTamanioPagina(int tamanioPagina) {
        this.tamanioPagina = tamanioPagina;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public boolean isUltima() {
        return ultima;
    }

    public void setUltima(boolean ultima) {
        this.ultima = ultima;
    }
}
