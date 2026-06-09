package com.CompraVenta.Backend.Modules.Employee.Repository;

import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import com.CompraVenta.Backend.Shared.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:search IS NUll OR 
            LOWER(e.fullName) LIKE LOWER(CONCAT('%',:search, '%')) OR 
            LOWER(e.email) LIKE LOWER(CONCAT('%', :search,'%')))
            AND (:active IS NULL OR e.active = :active)
            AND (:rol IS NULL OR e.rol =:rol)
""")
    Page<Employee> findByFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            @Param("rol") Role rol,
            Pageable pageable
    );
}
