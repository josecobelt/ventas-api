package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.domain.Cliente;
import com.pruebatecnica.ventas.dto.cliente.ClienteRequestDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteResponseDTO;
import com.pruebatecnica.ventas.exception.BusinessException;
import com.pruebatecnica.ventas.exception.DuplicateResourceException;
import com.pruebatecnica.ventas.exception.ResourceNotFoundException;
import com.pruebatecnica.ventas.repository.ClienteRepository;
import com.pruebatecnica.ventas.repository.VentaRepository;
import com.pruebatecnica.ventas.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    void crear_lanzaExcepcionSiEmailYaExiste() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Ana");
        dto.setEmail("ana@example.com");

        when(clienteRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> clienteService.crear(dto));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crear_guardaClienteCuandoEmailNoExiste() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Ana");
        dto.setEmail("ana@example.com");
        dto.setTelefono("555-1234");

        when(clienteRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClienteResponseDTO response = clienteService.crear(dto);

        assertEquals("Ana", response.getNombre());
        assertTrue(response.isActivo());
    }

    @Test
    void eliminar_bajaLogicaMarcaClienteComoInactivo() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setActivo(true);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.eliminar(1L, false);

        assertFalse(cliente.isActivo());
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void eliminar_permanenteLanzaExcepcionSiElClienteTieneVentas() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ventaRepository.existsByClienteId(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> clienteService.eliminar(1L, true));
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void obtenerPorId_lanzaExcepcionSiNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.obtenerPorId(99L));
    }
}
