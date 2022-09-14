package com.zs.library.my_health_pass_auth.configuration.annotations.enable_postgres_test_container;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestContainerContextFactory implements ContextCustomizerFactory {

  @Override
  public ContextCustomizer createContextCustomizer(
      @NotNull Class<?> testClass, @NotNull List<ContextConfigurationAttributes> configAttributes
  ) {
    if (!(AnnotatedElementUtils.hasAnnotation(testClass, PostgresTestContainer.class))) {
      return null;
    }

    return new PostgresTestContainerContextCustomizer();
  }

  @EqualsAndHashCode
  private static class PostgresTestContainerContextCustomizer implements ContextCustomizer {

    @Override
    public void customizeContext(
        ConfigurableApplicationContext context, @NotNull MergedContextConfiguration mergedConfig
    ) {

      val imageRepo = context.getEnvironment().getRequiredProperty("postgres.testcontainer.image.repository");
      val imageName = context.getEnvironment().getRequiredProperty("postgres.testcontainer.image.name");
      val imageTag = context.getEnvironment().getRequiredProperty("postgres.testcontainer.image.tag");

      val dockerImage = DockerImageName
          .parse(imageName)
          .withTag(imageTag)
          .withRegistry(imageRepo)
          .asCompatibleSubstituteFor("postgres");

      val postgresContainer = new PostgreSQLContainer<>(dockerImage);

      postgresContainer.start();

      val properties = Map.<String, Object>of(
          "spring.datasource.driver-class-name", "org.postgresql.Driver",
          "spring.datasource.username", postgresContainer.getUsername(),
          "spring.datasource.password", postgresContainer.getPassword(),
          "spring.datasource.url", postgresContainer.getJdbcUrl(),
          "spring.test.database.replace", "NONE"
      );

      val propertySource = new MapPropertySource("PostgresContainer Test Properties", properties);

      context.getEnvironment().getPropertySources().addFirst(propertySource);

    }

  }


}
