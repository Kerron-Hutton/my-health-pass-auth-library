package com.zs.library.my_health_pass_auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.zs.library.my_health_pass_auth.configuration.annotations.enable_postgres_test_container.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@PostgresTestContainer
@ActiveProfiles("test")
@SpringBootTest(classes = ApplicationTests.class)
class ApplicationTests {

  @Test
  void contextLoads() {
    assertThat(1 + 1).isEqualTo(2);
  }

}
