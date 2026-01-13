package com.jw.authorizationserver.config.jdbc;

import com.jw.authorizationserver.constants.BeanNameConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = BeanNameConstants.OAUTH_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource oauthDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = BeanNameConstants.OAUTH_JDBC_TEMPLATE)
    public JdbcTemplate oauthJdbcTemplate(@Qualifier(BeanNameConstants.OAUTH_DATA_SOURCE) DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = BeanNameConstants.RESOURCE_DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.secondary-datasource")
    public javax.sql.DataSource serviceDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = BeanNameConstants.RESOURCE_JDBC_TEMPLATE)
    public JdbcTemplate resourceJdbcTemplate(@Qualifier(BeanNameConstants.RESOURCE_DATA_SOURCE) DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
