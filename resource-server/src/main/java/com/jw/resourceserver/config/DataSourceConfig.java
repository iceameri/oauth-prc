package com.jw.resourceserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {
    @Bean(name = "resourceDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource serviceDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "resourceJdbcTemplate")
    public JdbcTemplate resourceJdbcTemplate(@Qualifier("resourceDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
