package com.zs.library.my_health_pass_auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.zs.library.my_health_pass_auth.pojo.ValidationError;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.val;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;

final class AppSecretUtil {

  public static final int JWT_SECRET_MAX_LENGTH_CONSTRAINT = 64;

  public static final int JWT_SECRET_MIN_LENGTH_CONSTRAINT = 32;

  public static final int PASSWORD_MAX_LENGTH_CONSTRAINT = 30;

  public static final int PASSWORD_MIN_LENGTH_CONSTRAINT = 8;

  private static final List<CharacterRule> BASE_RULES = List.of(
      // at least one upper-case or lower-case character
      new CharacterRule(EnglishCharacterData.Alphabetical, 1),

      // at least one symbol (special character)
      new CharacterRule(EnglishCharacterData.Special, 1),

      // at least one digit
      new CharacterRule(EnglishCharacterData.Digit, 1)
  );

  public static final List<Rule> PASSWORD_VALIDATION_RULES = new ImmutableList.Builder<Rule>()
      .add(new LengthRule(PASSWORD_MIN_LENGTH_CONSTRAINT, PASSWORD_MAX_LENGTH_CONSTRAINT))
      .addAll(BASE_RULES)
      .build();

  public static final List<Rule> JWT_SECRET_VALIDATION_RULES = new ImmutableList.Builder<Rule>()
      .add(new LengthRule(JWT_SECRET_MIN_LENGTH_CONSTRAINT, JWT_SECRET_MAX_LENGTH_CONSTRAINT))
      .addAll(BASE_RULES)
      .build();

  private AppSecretUtil() {
  }

  /**
   * Validates a given secret against specified rules.
   *
   * @param secret secret that requires validation
   * @param rules  rules to validate secret against
   * @return list of error messages if password fail
   */
  @SneakyThrows
  public static Optional<String> validateSecretAgainstRules(String secret, List<Rule> rules) {
    val validator = new PasswordValidator(rules);

    val result = validator.validate(new PasswordData(secret));

    val validationErrors = validator.getMessages(result).stream().map(ValidationError::new)
        .collect(Collectors.toList());

    if (validationErrors.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(validationErrors)
    );
  }
}
