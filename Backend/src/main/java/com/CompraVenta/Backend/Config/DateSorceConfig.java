package com.CompraVenta.Backend.Config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Date;

@Configuration
@EnableTransactionManagement
public class DateSorceConfig {
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
