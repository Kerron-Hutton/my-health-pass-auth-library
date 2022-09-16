package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.AppSecretUtil.PASSWORD_VALIDATION_RULES;

import com.zs.library.my_health_pass_auth.dto.UserAccountDetailsDto;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class IdentityManagement {

  private final UserEntityRepository repository;

  /**
   * Registers a new user in the MyHealthPass Application.
   *
   * @param accountDetails user information required to register user
   * @param password       user password required to register user
   * @return ID of newly registered user;
   */
  @Transactional
  public Long register(UserAccountDetailsDto accountDetails, String password) {
    val validationMessage = AppSecretUtil.validateSecretAgainstRules(
        password, PASSWORD_VALIDATION_RULES
    );

    if (validationMessage.isPresent()) {
      throw new RuntimeException(validationMessage.get());
    }

    if (accountDetails.getDateOfBirth().isAfter(LocalDate.now())) {
      throw new RuntimeException("Date of birth must not be in the future");
    }

    val passwordHash = UserPasswordUtil.generatePasswordHash(password);

    val userToBeRegistered = new UserEntity(accountDetails, passwordHash);

    val registeredUser = repository.save(userToBeRegistered);

    return registeredUser.getId();
  }

}
