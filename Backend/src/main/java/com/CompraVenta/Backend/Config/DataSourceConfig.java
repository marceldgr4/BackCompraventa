package com.CompraVenta.Backend.Config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")

    public DataSourceProperties dataSourceProperties(){
    return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties){
        return properties.initializeDataSourceBuilder()
                .type(properties.getType())
                .build();
    }
}
