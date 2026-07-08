package com.CompraVenta.Backend.Modules.Pawns.Entity;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Clients.Entity.Cliente;
import com.CompraVenta.Backend.Modules.Pawns.Enums.PawnStatus;
import com.CompraVenta.Backend.Shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Builder
@Entity
@Table(name = "pawns", schema = "public", indexes = {@Index(
        name = "idx_pawn_global_id_lookup",columnList = "global_id",unique = true
)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pawn extends BaseEntity {
    private static final Set<PawnStatus> TERMINAL_STATE = EnumSet.of(
            PawnStatus.FINALIZADO, PawnStatus.RETIRADO, PawnStatus.PERDIDO, PawnStatus.VENDIDO
    );
    private static final Set<PawnStatus> PAYABLE_STATES = EnumSet.of(
            PawnStatus.ACTIVO, PawnStatus.VENCIDO
    );
    private static final int MAX_INSTALLMENTS_MISSED = 4;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id",nullable = false, updatable = false,insertable = false)
    private Article article;
    @Column(name = "article_id",nullable = false)
    private Long articleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id",nullable = false,updatable = false,insertable = false)
    private Cliente cliente;
    @Column(name = "cliente_id",nullable = false)
    private Long clienteId;

    @Column(name = "amount",nullable = false)
    private Integer amount;

    @Column(name = "price",nullable = false,precision = 12,scale = 2)
    private BigDecimal price;

    @Column(name = "weight_grams", precision = 10, scale = 2)
    private BigDecimal weightGrams;

    @Column(name = "installment_count", nullable = false)
    @Builder.Default
    private Integer installmentCount = 1;

    @Column(name = "installments_paid", nullable = false)
    @Builder.Default
    private Integer installmentsPaid = 0;

    @Column(name = "installments_missed", nullable = false)
    @Builder.Default
    private Integer installmentsMissed = 0;

    @Column(name = "pawn_date", nullable = false)
    private LocalDate pawnDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PawnStatus status = PawnStatus.ACTIVO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    //--Mqunia de estado

    public boolean canAcceptPayments(){
        return PAYABLE_STATES.contains(this.status);
    }
    public boolean isTerminalState(){
        return TERMINAL_STATE.contains(this.status);
    }

    public boolean canBeMarkedReturned(){
        return  PAYABLE_STATES.contains(this.status);
    }

    public void validateStateTransition(PawnStatus newStatus){
        if(isTerminalState()){
            throw new BusinessException(String.format(
                    "El empeño '%s' esta en estado terminal '%s' y no puede modificarse.",
                    getGlobalId(),this.status
            ));
        }
        if(this.status == newStatus){
            throw new BusinessException(String.format(
                    "El empaño ya se encuntra en estado '%s'.", newStatus
            ));
        }
    }
    public void registerPayment(BigDecimal paymentAmount){
        if(!canBeMarkedReturned()){
            throw new BusinessException(String.format(
                    "el empeño '%s' en estdo '%s' no aceptta pagos.", getGlobalId(), this.status
            ));
        }
        this.installmentsPaid = installmentsPaid + 1;
        if(this.installmentsPaid >= this.installmentCount){
            this.status = PawnStatus.FINALIZADO;
        }
    }
    public void registerMissedInstallments(){
        if(!canAcceptPayments()){
            throw new BusinessException(String.format(
                    "El empeño '%s' en estado '%s' no puede registrar cuotas impagadas.",
                    getGlobalId(), this.status
            ));
        }
        this.installmentsMissed = installmentsMissed + 1;
        if(this.installmentsMissed > MAX_INSTALLMENTS_MISSED){
            this.status = PawnStatus.PERDIDO;
        }
    }
    public void markAsReturned() {
        validateStateTransition(PawnStatus.RETIRADO);
        if (!canBeMarkedReturned()) {
            throw new BusinessException(String.format(
                    "El empeño '%s' en estado '%s' no puede marcarse como devuelto.",
                    getGlobalId(), this.status));
        }
        this.status = PawnStatus.RETIRADO;
    }

    public void markAsLost() {
        validateStateTransition(PawnStatus.PERDIDO);
        this.status = PawnStatus.PERDIDO;
    }

    public void markAsExpired() {
        if (this.status == PawnStatus.ACTIVO) {
            this.status = PawnStatus.VENCIDO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pawn p)) return false;
        return getId() != null && getId().equals(p.getId());
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
