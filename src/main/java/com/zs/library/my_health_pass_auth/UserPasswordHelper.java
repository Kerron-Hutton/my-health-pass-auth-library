package com.zs.library.my_health_pass_auth;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.val;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;

final class UserPasswordHelper {

  public static final int PASSWORD_MAX_CONSTRAINT = 30;

  public static final int PASSWORD_MIN_CONSTRAINT = 8;

  private UserPasswordHelper() {
  }

  /**
   * Generates SHA265 hash of specified user password.
   *
   * @param password user password that requires hashing
   * @return SHA256 hash of the user password
   */
  public static String generatePasswordHash(String password) {
    return Hashing.sha256()
        .hashString(password, StandardCharsets.UTF_8)
        .toString();
  }

  /**
   * Validates a given password against an already hashed password.
   *
   * @param password     user password that requires validation
   * @param passwordHash user password hash to test current password against
   * @return boolean that represents validation success
   */
  public static Boolean validatePasswordAgainstHash(String password, String passwordHash) {
    val currentPasswordHash = generatePasswordHash(password);
    return currentPasswordHash.equals(passwordHash);
  }

  /**
   * Validates a given password against defined password rules.
   *
   * @param password user password that requires validation
   * @return list of error messages if password fail
   */
  public static String validatePasswordStrength(String password) {
    val validator = new PasswordValidator(List.of(
        // at least 8 characters
        new LengthRule(PASSWORD_MIN_CONSTRAINT, PASSWORD_MAX_CONSTRAINT),

        // at least one upper-case character
        new CharacterRule(EnglishCharacterData.UpperCase, 1),

        // at least one symbol (special character)
        new CharacterRule(EnglishCharacterData.Digit, 1)
    ));

    val result = validator.validate(new PasswordData(password));

    return String.join(",", validator.getMessages(result));
  }

}
