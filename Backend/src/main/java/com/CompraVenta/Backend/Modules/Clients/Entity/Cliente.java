package com.CompraVenta.Backend.Modules.Clients.Entity;

import com.CompraVenta.Backend.Shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

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
}
