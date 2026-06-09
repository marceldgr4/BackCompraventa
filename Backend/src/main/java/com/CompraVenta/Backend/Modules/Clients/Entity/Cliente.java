package com.CompraVenta.Backend.Modules.Clients.Entity;

import com.CompraVenta.Backend.Modules.Clients.Emus.ClienteStatus;
import com.CompraVenta.Backend.Modules.Clients.Emus.RegistrationType;
import com.CompraVenta.Backend.Shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.actuate.health.Status;

@Entity
@Table(
        name = "clientes",
        schema= "public",
        indexes = {
                @Index(name = "idx_clientes_global_id", columnList = "global_id",
                unique = true),
                @Index(name = "idx_clientes_cedula", columnList = "cedula"),
                @Index(name = "idx_cliente_status", columnList = "status")

        }

)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends BaseEntity {
    @Column(name = "cedula", unique = true, length =20)
    private String cedula;

    @Column(name = "first_name", nullable = false,length = 100)
    private String firstName;

    @Column(name = "last_name",length = 100)
    private String lastName;

    @Column(name = "email",length = 255)
    private String email;

    @Column(name = "phone",length = 255)
    private String phone;

    @Column(name = "addres", length = 255)
    private String address;

    @Column(name = "city", length = 255)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,  length = 20)
    @Builder.Default
    private ClienteStatus status = ClienteStatus.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "register_type", nullable = false, length =20 )
    @Builder.Default
    private RegistrationType registrationType = RegistrationType.COMPLETO;

    public boolean isActive() {
        return ClienteStatus.ACTIVO.equals(status);
    }
    public boolean isCompleteRegistration() {
        return RegistrationType.COMPLETO.equals(this.registrationType);
    }
    public void promoteComplete(){
        if(RegistrationType.RAPIDO.equals(this.registrationType)
         && this.lastName != null && this.lastName.isBlank()){
            this.registrationType = RegistrationType.COMPLETO;
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Cliente c)) return false;
        return getId() != null && getId().equals(c.getId());
    }
    @Override
    public int hashCode() {
        return  getClass().hashCode();
    }
}
