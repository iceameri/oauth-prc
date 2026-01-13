package com.jw.resourceserver.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.jw.resourceserver.repository",
        entityManagerFactoryRef = "resourceEntityManagerFactory",
        transactionManagerRef = "resourceTransactionManager"
)
public class JpaResourceConfig {
    @Bean(name = "resourceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean resourceEntityManagerFactory(
            @Qualifier("resourceDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.jw.resourceserver.entity.resource")   // JPA Entity 위치
                .persistenceUnit("resource")
                .build();
    }

    @Bean(name = "resourceTransactionManager")
    public PlatformTransactionManager resourceTransactionManager(
            @Qualifier("resourceEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
