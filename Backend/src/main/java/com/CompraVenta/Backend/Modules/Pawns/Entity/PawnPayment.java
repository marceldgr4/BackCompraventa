package com.CompraVenta.Backend.Modules.Pawns.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Builder
@Entity
@Table(
        name = "pawn_paymnts", schema = "public",
        indexes = {
                @Index(name = "idx_pawn_payments_pawn_id_lookup",
                columnList = "pawn_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PawnPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pawn_id",nullable = false,updatable = false,insertable = false)
    private Pawn pawn;

    @Column(name = "pawn_id",nullable = false)
    private Long pawnId;

    @Column(name = "amount",nullable = false,precision = 12,scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "payment-date",nullable = false)
    private LocalDate paymentDate;

    @Column(name = "notes",columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by_employee_id")
    private Long createdByEmployeeId;

    @Column(name = "is_missed",nullable = false)
    @Builder.Default
    private Boolean missed = false;

    @Column(name = "created_at",nullable = false,updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
