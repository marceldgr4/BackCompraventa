package com.CompraVenta.Backend.Audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;



@Entity
@Table(name = "audit_log",schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AudLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name="operation", nullable = false, length = 100)
    private String operation;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value",columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "ip_address",length = 45)
    private String ipAddress;

    @Column(name = "timestamp",nullable = false)
    private Instant timestamp;

}
