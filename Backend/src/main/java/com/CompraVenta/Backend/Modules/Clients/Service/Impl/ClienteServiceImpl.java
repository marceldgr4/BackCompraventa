package com.CompraVenta.Backend.Modules.Clients.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Emus.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Mapper.ClienteMapper;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Modules.Clients.Service.ClienteService;
import com.CompraVenta.Backend.Shared.Dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> findAll(ClienteStatus status, Pageable pageable){
        return PageResponse.from(
                (status !=null
                ? clienteRepository.findAllByStatus(status,pageable)
                : clienteRepository.findAll(pageable))
                .map(clienteMapper::toClienteResponse)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findByGlobalId(UUID globalId){
        return clienteRepository.findByGloblaId(globalId)
                .map(clienteMapper::toClienteResponse)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente",globalId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> search(String term, ClienteStatus status, Pageable pageable){
        return PageResponse.from(
                clienteRepository.searchByTerm(term,status,pageable)
                .map(clienteMapper::toClienteResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse create(CreateClienteRequest request) {
        if(request.cedula() !=null && !request.cedula().isBlank() && clienteRepository.existsByCedula(request.cedula())){
            throw new BusinessException(
                    "Ya existe un cliente con numero de cedula " + request.cedula()
            );
        }
        Cliente cliente = ClienteMapper.toEntity(request);
        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente creado id={}, globalId={}, tipo={}", saved.getId(),
                saved.getGlobalId(), saved.getRegistrationType());
        return clienteMapper.toClienteResponse(saved);
    }

    @Override
    @Transactional
    public ClienteResponse update(UUID globalId, UpdateClienteRequest request) {
        Cliente cliente = findEntityOrThrow(globalId);
                if(request.cedula() !=null && !request.cedula().isBlank()
                && clienteRepository.exitsByCedulaAndIdNot(request.cedula(), cliente.getId())){
                    throw new BusinessException(
                            "Ya existe un Cliente Con la cedular"+ request.cedula());
                }
                clienteMapper.applyUpdates(cliente, request);
                Cliente saved = clienteRepository.save(cliente);
                log.info("Cliente actulizaod: globalId={},tipo={}", globalId, saved.getRegistrationType());
        return clienteMapper.toClienteResponse(saved);
    }

    private Cliente findEntityOrThrow(UUID globalId) {
        return clienteRepository.findByGloblaId(globalId)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente",globalId));
    }

    @Override
    @Transactional
    public void delete(UUID globalId) {
        Cliente cliente = findEntityOrThrow(globalId);
        cliente.setStatus(ClienteStatus.INACTIVO);
        clienteRepository.save(cliente);
        log.info("Cliente eliminado: globalId={}", globalId);

    }

}
