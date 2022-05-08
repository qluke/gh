package com.luke.spring.boot.autoconfigure.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    DataSource dataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.history")
    @Qualifier("historyDataSource")
    DataSource historyDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    JdbcTemplate jdbcTemplateWrite(@Qualifier("historyDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

