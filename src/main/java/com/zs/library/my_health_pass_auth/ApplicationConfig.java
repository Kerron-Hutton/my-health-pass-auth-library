package com.zs.library.my_health_pass_auth;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
class ApplicationConfig {

  @Bean
  public SpringLiquibase liquibase(DataSource dataSource) {
    val liquibase = new SpringLiquibase();

    liquibase.setChangeLog("classpath:/db/liquibase-change-log.xml");
    liquibase.setDataSource(dataSource);

    return liquibase;
  }

  @Bean
  public EntityManagerFactory entityManagerFactory(DataSource dataSource) {

    val vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(true);

    val factory = new LocalContainerEntityManagerFactoryBean();

    factory.setPackagesToScan("com.zs.library.my_health_pass_auth");
    factory.setJpaVendorAdapter(vendorAdapter);

    factory.setDataSource(dataSource);
    factory.afterPropertiesSet();

    return factory.getObject();
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
    val transactionManager = new JpaTransactionManager();

    transactionManager.setEntityManagerFactory(factory);

    return transactionManager;
  }


  @Bean
  public IdentityManagement identityManagementBean(EntityManager entityManager) {
    val factory = new JpaRepositoryFactory(entityManager);

    val userRepository = factory.getRepository(UserEntityRepository.class);

    return new IdentityManagement(userRepository);
  }

}