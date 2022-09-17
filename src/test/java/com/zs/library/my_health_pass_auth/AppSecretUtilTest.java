package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.AppSecretUtil.PASSWORD_VALIDATION_RULES;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("App Secret Test")
class AppSecretUtilTest {

  @Test
  @DisplayName("It should pass if all password criteria is met")
  void itShouldPassPasswordStrengthValidation() {
    // Given
    String password = PasswordGeneratorUtil.generateValidPassword(
        PASSWORD_VALIDATION_RULES
    );

    // When
    Optional<String> validationMessage = AppSecretUtil.validateSecretAgainstRules(
        password, PASSWORD_VALIDATION_RULES
    );

    // Then
    assertThat(validationMessage).isNotPresent();
  }

  @Test
  @DisplayName("It should fail if password criteria is not met")
  void itShouldFailPasswordStrengthValidation() {
    // Given
    String password = PasswordGeneratorUtil.generateInValidPassword(
        PASSWORD_VALIDATION_RULES
    );

    // When
    Optional<String> validationMessage = AppSecretUtil.validateSecretAgainstRules(
        password, PASSWORD_VALIDATION_RULES
    );

    // Then
    assertThat(validationMessage).isPresent();
  }
}