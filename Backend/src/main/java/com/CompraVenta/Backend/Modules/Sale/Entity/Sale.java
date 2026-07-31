package com.CompraVenta.Backend.Modules.Sale.Entity;

import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Employee.Entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Entity
@Table( name = "sales", schema = "public",
        indexes = {
                @Index(name ="idx_sales_global_id",columnList = "global_id", unique = true),
                @Index(name = "idx_sales_employee",columnList = "employee_id"),
                @Index(name = "idx_sales_data", columnList = "sale_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "global_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID globalId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, insertable = false, updatable = false)
    private Employee employee;

    @Column(name = "employee_id",nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    private Cliente cliente;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nombre_anon")
    private String clienteNombreAnon;

    @Column(name = "sale_date",nullable = false)
    private LocalDate saleDate;

    @Column(name = "notes",columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    public boolean isAnonymous() {
        return clienteId == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sale s)) return false;
        return getId() != null && getId().equals(s.getId());
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
