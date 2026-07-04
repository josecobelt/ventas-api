package com.pruebatecnica.ventas.service.impl;

import com.pruebatecnica.ventas.domain.Cliente;
import com.pruebatecnica.ventas.dto.cliente.ClienteRequestDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import com.pruebatecnica.ventas.exception.BusinessException;
import com.pruebatecnica.ventas.exception.DuplicateResourceException;
import com.pruebatecnica.ventas.exception.ResourceNotFoundException;
import com.pruebatecnica.ventas.mapper.ClienteMapper;
import com.pruebatecnica.ventas.repository.ClienteRepository;
import com.pruebatecnica.ventas.repository.VentaRepository;
import com.pruebatecnica.ventas.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, VentaRepository ventaRepository) {
        this.clienteRepository = clienteRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        if (clienteRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new DuplicateResourceException("Ya existe un cliente con el email: " + dto.getEmail());
        }
        Cliente cliente = ClienteMapper.toEntity(dto);
        cliente.setActivo(true);
        Cliente guardado = clienteRepository.save(cliente);
        return ClienteMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = obtenerEntidadPorId(id);

        boolean emailCambio = !cliente.getEmail().equalsIgnoreCase(dto.getEmail());
        if (emailCambio && clienteRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new DuplicateResourceException("Ya existe un cliente con el email: " + dto.getEmail());
        }

        ClienteMapper.actualizarEntity(cliente, dto);
        return ClienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        return ClienteMapper.toResponseDTO(obtenerEntidadPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ClienteResponseDTO> listar(Pageable pageable, boolean soloActivos) {
        Page<Cliente> page = soloActivos
                ? clienteRepository.findByActivoTrue(pageable)
                : clienteRepository.findAll(pageable);
        return new PageResponseDTO<>(page.map(ClienteMapper::toResponseDTO));
    }

    @Override
    @Transactional
    public void eliminar(Long id, boolean permanente) {
        Cliente cliente = obtenerEntidadPorId(id);

        if (permanente) {
            if (ventaRepository.existsByClienteId(id)) {
                throw new BusinessException(
                        "No se puede eliminar físicamente un cliente con ventas registradas. Use baja lógica.");
            }
            clienteRepository.delete(cliente);
        } else {
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        }
    }

    private Cliente obtenerEntidadPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }
}
