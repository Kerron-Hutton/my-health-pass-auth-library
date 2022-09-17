package com.zs.library.my_health_pass_auth;

import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.passay.CharacterRule;
import org.passay.LengthRule;
import org.passay.PasswordGenerator;
import org.passay.Rule;

final class PasswordGeneratorUtil {

  private PasswordGeneratorUtil() {
  }

  public static String generateValidPassword(List<Rule> rules) {
    val generator = new PasswordGenerator();

    val characterRules = rules.stream()
        .filter(rule -> rule instanceof CharacterRule)
        .map(rule -> (CharacterRule) rule)
        .collect(Collectors.toList());

    val lengthRule = (LengthRule) rules.stream()
        .filter(rule -> rule instanceof LengthRule)
        .findFirst().orElseThrow();

    return generator.generatePassword(
        lengthRule.getMinimumLength(), characterRules
    );
  }

  public static String generateInValidPassword(List<Rule> rules) {
    val generator = new PasswordGenerator();

    val characterRules = rules.stream()
        .filter(rule -> rule instanceof CharacterRule)
        .map(rule -> (CharacterRule) rule)
        .collect(Collectors.toList());

    val lengthRule = (LengthRule) rules.stream()
        .filter(rule -> rule instanceof LengthRule)
        .findFirst().orElseThrow();

    return generator.generatePassword(
        lengthRule.getMinimumLength() - 1, characterRules
    );
  }
}
