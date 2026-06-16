package com.CompraVenta.Backend.Modules.Clients.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Exception.custom.ResourceNotFoundException;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.CreateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Request.UpdateClienteRequest;
import com.CompraVenta.Backend.Modules.Clients.Dto.Response.ClienteResponse;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Enums.RegistrationType;
import com.CompraVenta.Backend.Modules.Clients.Mapper.ClienteMapper;
import com.CompraVenta.Backend.Modules.Clients.Repository.ClienteRepository;
import com.CompraVenta.Backend.Security.context.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl — Tests Unitarios")
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private UUID globalId;
    private Cliente clienteActivo;
    private ClienteResponse clienteResponse;

    @BeforeEach
    void setUp() {
        globalId = UUID.randomUUID();

        clienteActivo = Cliente.builder()
                .cedula("1234567890")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phone("3001234567")
                .address("Calle 1")
                .city("Bogotá")
                .registrationType(RegistrationType.COMPLETO)
                .build();

        // Simulamos que la entidad tiene ID asignado por JPA
        // (Lombok @Builder no llama a BaseEntity, así que usamos reflection o spy)
        clienteResponse = new ClienteResponse(
                1L, globalId,
                "1234567890", "Juan", "Pérez",
                "juan@example.com", "3001234567",
                "Calle 1", "Bogotá",
                ClienteStatus.ACTIVO, RegistrationType.COMPLETO,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // ── Test 1: findAll como ADMIN sin filtro → devuelve todos ────────────────

    @Test
    @DisplayName("T01 — findAll: Admin sin filtro retorna todos los clientes")
    void findAll_adminSinFiltro_retornaTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> pageEntidad = new PageImpl<>(List.of(clienteActivo));

        try (MockedStatic<SecurityContext> secCtx = mockStatic(SecurityContext.class)) {
            secCtx.when(() -> SecurityContext.hasRole("ADMIN")).thenReturn(true);

            when(clienteRepository.findAll(pageable)).thenReturn(pageEntidad);
            when(clienteMapper.toClienteResponse(clienteActivo)).thenReturn(clienteResponse);

            var result = clienteService.findAll(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(clienteRepository).findAll(pageable);
            verify(clienteRepository, never()).findAllByStatus(any(), any());
        }
    }

    // ── Test 2: findAll como Empleado → siempre filtra por ACTIVO ─────────────

    @Test
    @DisplayName("T02 — findAll: Empleado recibe solo clientes ACTIVO independiente del filtro")
    void findAll_empleado_filtraAutomaticamentePorActivo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> pageEntidad = new PageImpl<>(List.of(clienteActivo));

        try (MockedStatic<SecurityContext> secCtx = mockStatic(SecurityContext.class)) {
            secCtx.when(() -> SecurityContext.hasRole("ADMIN")).thenReturn(false);

            when(clienteRepository.findAllByStatus(ClienteStatus.ACTIVO, pageable))
                    .thenReturn(pageEntidad);
            when(clienteMapper.toClienteResponse(clienteActivo)).thenReturn(clienteResponse);

            // Empleado pasa ELIMINADO, pero debe recibir solo ACTIVO
            var result = clienteService.findAll(ClienteStatus.ELIMINADO, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(clienteRepository).findAllByStatus(ClienteStatus.ACTIVO, pageable);
            verify(clienteRepository, never()).findAll(any(Pageable.class));
        }
    }

    // ── Test 3: create exitoso ─────────────────────────────────────────────────

    @Test
    @DisplayName("T03 — create: Crea cliente cuando cédula y teléfono son únicos")
    void create_datosUnicos_creaClienteExitosamente() {
        var request = new CreateClienteRequest(
                "1234567890", "Juan", "Pérez",
                "juan@example.com", "3001234567",
                "Calle 1", "Bogotá"
        );

        when(clienteRepository.existsByCedulaAndStatusNot("1234567890", ClienteStatus.ELIMINADO))
                .thenReturn(false);
        when(clienteRepository.existsByPhoneAndStatusNot("3001234567", ClienteStatus.ELIMINADO))
                .thenReturn(false);
        when(clienteMapper.toEntity(request)).thenReturn(clienteActivo);
        when(clienteRepository.save(clienteActivo)).thenReturn(clienteActivo);
        when(clienteMapper.toClienteResponse(clienteActivo)).thenReturn(clienteResponse);

        var result = clienteService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.cedula()).isEqualTo("1234567890");
        verify(clienteRepository).save(clienteActivo);
    }

    // ── Test 4: create con cédula duplicada → lanza BusinessException ──────────

    @Test
    @DisplayName("T04 — create: Lanza BusinessException si cédula ya existe en cliente activo")
    void create_cedulaDuplicada_lanzaBusinessException() {
        var request = new CreateClienteRequest(
                "1234567890", "Otro", "Usuario",
                null, null, null, null
        );

        when(clienteRepository.existsByCedulaAndStatusNot("1234567890", ClienteStatus.ELIMINADO))
                .thenReturn(true);

        assertThatThrownBy(() -> clienteService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cédula");

        verify(clienteRepository, never()).save(any());
    }

    // ── Test 5: create con teléfono duplicado → lanza BusinessException ────────

    @Test
    @DisplayName("T05 — create: Lanza BusinessException si teléfono ya existe en cliente activo")
    void create_telefonoDuplicado_lanzaBusinessException() {
        var request = new CreateClienteRequest(
                null, "Otro", "Usuario",
                null, "3001234567", null, null
        );

        when(clienteRepository.existsByPhoneAndStatusNot("3001234567", ClienteStatus.ELIMINADO))
                .thenReturn(true);

        assertThatThrownBy(() -> clienteService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("teléfono");

        verify(clienteRepository, never()).save(any());
    }

    // ── Test 6: delete — soft-delete cambia status a ELIMINADO ────────────────

    @Test
    @DisplayName("T06 — delete: Cambia status a ELIMINADO (soft-delete)")
    void delete_clienteExistente_cambiasStatusEliminado() {
        when(clienteRepository.findByGlobalId(globalId)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(clienteActivo)).thenReturn(clienteActivo);

        clienteService.delete(globalId);

        assertThat(clienteActivo.getStatus()).isEqualTo(ClienteStatus.ELIMINADO);
        verify(clienteRepository).save(clienteActivo);
        verify(clienteRepository, never()).delete(any());
    }

    // ── Test 7: findByGlobalId — cliente no encontrado → ResourceNotFoundException

    @Test
    @DisplayName("T07 — findByGlobalId: Lanza ResourceNotFoundException si el UUID no existe")
    void findByGlobalId_noExiste_lanzaResourceNotFoundException() {
        when(clienteRepository.findByGlobalId(globalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findByGlobalId(globalId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteMapper, never()).toClienteResponse(any());
    }
}
