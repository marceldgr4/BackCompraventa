package com.CompraVenta.Backend.Modules.Employee.Entity;

import com.CompraVenta.Backend.Shared.entity.BaseEntity;
import com.CompraVenta.Backend.Shared.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Entity
@Table(
        name= "employees",
        schema = "public",
        indexes = {
                @Index(name = "idx_employees_email", columnList = "email", unique = true),
                @Index(name = "idx_employees_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Employee extends BaseEntity {

    @Column(name = "email",nullable = false,unique = true,length = 255)
    private String email;

    @Column(name = "full_name", nullable = false,length = 255)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false,length = 20)
    private Role rol;

    @Column(name = "active",nullable = false)
    @Builder.Default
    private boolean active = true;


    public boolean isAdmin() {
        return Role.ADMIN.equals(this.rol);
    }

    public boolean canAuthenticate() {
        return this.active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Employee e)) return false;
        return email != null && email.equalsIgnoreCase(e.email);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
