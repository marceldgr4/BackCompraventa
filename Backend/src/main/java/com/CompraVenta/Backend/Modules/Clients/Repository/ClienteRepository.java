package com.CompraVenta.Backend.Modules.Clients.Repository;

import com.CompraVenta.Backend.Modules.Clients.Enums.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCedula(String cedula);
    Optional<Cliente> findByPhone(String phone);

    boolean existsByCedula(String cedula);
    boolean existsByCedulaAndIdNot(String cedula, long id);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    Optional<Cliente> findByGlobalId(UUID globalId);
    Page<Cliente> findAllByStatus(ClienteStatus status, Pageable pageable);

    @Query("""
            SELECT c FROM Cliente c
            WHERE(
                    LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%'))
                    OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :term, '%'))
                    OR LOWER(c.cedula) LIKE LOWER(CONCAT('%', :term, '%'))
                    OR LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%'))
            ) AND(:status IS NULL OR c.status = :status)
        """)
    Page<Cliente> searchByTerm(
            @Param("term") String term,
            @Param("status") ClienteStatus status,
            Pageable pageable
    );
}
