package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.AppSecretUtil.PASSWORD_VALIDATION_RULES;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.AUTHENTICATION_ACCOUNT_LOCK_DURATION;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.AUTHENTICATION_MAX_LOGIN_ATTEMPT;

import com.zs.library.my_health_pass_auth.dto.UserAccountDetailsDto;
import com.zs.library.my_health_pass_auth.dto.UserIdentityDto;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import com.zs.library.my_health_pass_auth.pojo.FileDocument;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class IdentityManagement {

  private final UserEntityRepository repository;

  private final FileServerUtil fileServerUtil;

  private final JwtTokenUtil jwtTokenUtil;

  private final Environment environment;

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

    if (accountDetails.getFileDocument() != null) {
      fileServerUtil.writeFileToServer(accountDetails.getFileDocument());
    }

    return registeredUser.getId();
  }

  /**
   * Validates the signature of provided token.
   *
   * @param token token given to user after successful login
   * @return user identity if token is validX
   */
  public UserIdentityDto authenticate(String token) {
    return jwtTokenUtil.getUserIdentityIfTokenIsValid(token);
  }

  /**
   * Authenticate user their credentials.
   *
   * @param username user defined application identifier.
   * @param password user secret credentials required for identification
   * @return valid token if authentication is successful
   */
  @Transactional
  public Optional<String> login(String username, String password) {
    val user = repository.findByUsername(username).orElse(null);

    if (user == null) {
      return Optional.empty();
    }

    handleAccountLockStatus(user);

    val isPasswordValid = UserPasswordUtil.validatePasswordAgainstHash(
        password, user.getPassword()
    );

    if (isPasswordValid) {
      val userIdentity = UserIdentityDto.builder()
          .firstName(user.getFirstName())
          .lastName(user.getLastName())
          .username(user.getUsername())
          .id(user.getId())
          .build();

      user.setAccountLockTimestamp(null);
      user.setAccountLocked(false);
      user.setFailedLogins(0);

      return Optional.of(
          jwtTokenUtil.generateUserAuthToken(userIdentity)
      );
    }

    user.setFailedLogins(user.getFailedLogins() + 1);

    val maxLoginAttempt = Integer.parseInt(
        environment.getRequiredProperty(AUTHENTICATION_MAX_LOGIN_ATTEMPT)
    );

    if (user.getFailedLogins() >= maxLoginAttempt) {
      user.setAccountLockTimestamp(LocalDateTime.now());
      user.setAccountLocked(true);
    }

    repository.save(user);

    return Optional.empty();
  }

  /**
   * Retrieves user profile from file server if present.
   *
   * @param userId user id to retrieve profile
   * @return file document containing profile picture data
   */
  public Optional<FileDocument> getUserProfilePicture(Long userId) {
    val user = repository.findById(userId);

    if (user.isEmpty() || user.get().getProfilePicture() == null) {
      return Optional.empty();
    }

    val document = fileServerUtil.readFileFromServer(
        user.get().getProfilePicture()
    );

    return Optional.of(document);
  }

  private void handleAccountLockStatus(UserEntity user) {
    val accountLockTimeout = Integer.parseInt(
        environment.getRequiredProperty(AUTHENTICATION_ACCOUNT_LOCK_DURATION)
    );

    if (user.isAccountLocked()) {
      val durationSinceAccountLock = Duration.between(
              user.getAccountLockTimestamp(), LocalDateTime.now()
          )
          .toMinutes();

      if (durationSinceAccountLock > 0 && durationSinceAccountLock < accountLockTimeout) {
        throw new RuntimeException(
            String.format("User account is locked... Please try again in %s minutes",
                accountLockTimeout - durationSinceAccountLock
            )
        );
      }

      user.setAccountLockTimestamp(null);
      user.setAccountLocked(false);
      user.setFailedLogins(0);
    }
  }

}
