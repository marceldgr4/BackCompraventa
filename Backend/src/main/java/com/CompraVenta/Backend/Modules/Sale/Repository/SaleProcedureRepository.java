package com.CompraVenta.Backend.Modules.Sale.Repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.List;



@Repository
@RequiredArgsConstructor
public class SaleProcedureRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public record SaleItemPayload(
            @JsonProperty("article_id") Long articleId,
            @JsonProperty("amount") Integer amount,
            @JsonProperty("unit_price")BigDecimal unitPrice
            ){ }
    public  Long registerSale(Long employeeId, Long clienteId, String clienteNombreAnon,
                              String notes, List<SaleItemPayload> items){
        String itemsJson = toJson(items);

        return jdbcTemplate.execute((Connection connection)-> {
            try (CallableStatement cs = connection.prepareCall("{call register_sale(?,?,?,?,?)}")){
                cs.setLong(1 , employeeId);
                if(clienteId != null){
                    cs.setLong(2,clienteId);
                }else{
                    cs.setNull(2, Types.BIGINT);
                }
                cs.setString(3, clienteNombreAnon);
                cs.setString(4, notes);

                PGobject itemsPayload = new PGobject();
                itemsPayload.setType("json");
                itemsPayload.setValue(itemsJson);
                cs.setObject(5, itemsPayload);

                cs.registerOutParameter(6, Types.BIGINT);
                cs.execute();
                return cs.getLong(6);
            }
        });
    }
    private String toJson(List<SaleItemPayload> items){
        try {
            return objectMapper.writeValueAsString(items);
        }catch (JsonProcessingException e){
            throw new IllegalStateException ("no se puede serializar los item de la venta :" +e.getMessage());
        }
    }
}
