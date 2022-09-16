package com.zs.library.my_health_pass_auth;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import lombok.val;

final class UserPasswordUtil {

  private UserPasswordUtil() {
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

}
