package com.zs.library.my_health_pass_auth;

import com.zs.library.my_health_pass_auth.dto.UserAccountDetailsDto;
import com.zs.library.my_health_pass_auth.dto.UserIdentityDto;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import com.zs.library.my_health_pass_auth.pojo.ApiRequestSignature;
import com.zs.library.my_health_pass_auth.pojo.FileDocument;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class IdentityManagement {

  private final IdentityManagementHelper helper;

  private final RegionRepository regionRepository;

  private final UserEntityRepository repository;

  private final FileServerUtil fileServerUtil;

  private final JwtTokenUtil jwtTokenUtil;

  /**
   * Registers a new user in the MyHealthPass Application.
   *
   * @param accountDetails user information required to register user
   * @param password       user password required to register user
   * @return ID of newly registered user;
   */
  @Transactional
  public Long register(UserAccountDetailsDto accountDetails, String password) {
    val region = regionRepository.findByRegionCode(accountDetails.getRegionCode());

    val validationMessage = AppSecretUtil.validateSecretAgainstRegionRules(
        password, region
    );

    if (validationMessage.isPresent()) {
      throw new RuntimeException(validationMessage.get());
    }

    if (accountDetails.getDateOfBirth().isAfter(LocalDate.now())) {
      throw new RuntimeException("Date of birth must not be in the future");
    }

    val passwordHash = UserPasswordUtil.generatePasswordHash(password);

    val userToBeRegistered = new UserEntity(accountDetails, passwordHash);

    userToBeRegistered.setRegion(region);

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
   * @param username  user defined application identifier.
   * @param password  user secret credentials required for identification
   * @param signature unique signature base on api request
   * @return valid token if authentication is successful
   */
  @Transactional()
  public Optional<String> login(String username, String password, ApiRequestSignature signature) {
    val user = repository.findByUsername(username).orElse(null);

    if (user == null) {
      helper.handleFailedLoginRequest(signature);
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
          jwtTokenUtil.generateUserAuthToken(userIdentity, user.getRegion())
      );
    } else {
      val shouldContinue = helper.handleFailedLoginRequest(signature);

      if (!shouldContinue) {
        return Optional.empty();
      }
    }

    user.setFailedLogins(user.getFailedLogins() + 1);

    val maxLoginAttempt = user.getRegion().getMaxFailedLogin();

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

  /**
   * Remove user profile picture from the database and file server.
   *
   * @param userId user id to remove profile picture
   * @return status of profile deletion
   */
  @Transactional
  public boolean removeUserProfilePicture(Long userId) {
    val user = repository.findById(userId);

    if (user.isEmpty() || user.get().getProfilePicture() == null) {
      return false;
    }

    val profilePicture = user.get().getProfilePicture();

    user.get().setProfilePicture(null);

    repository.save(user.get());

    fileServerUtil.deleteFileFromServer(profilePicture);

    return true;
  }

  private void handleAccountLockStatus(UserEntity user) {
    val accountLockTimeout = user.getRegion().getAccountLockDuration();

    if (user.isAccountLocked()) {
      val durationSinceAccountLock = Duration.between(
              user.getAccountLockTimestamp(), LocalDateTime.now()
          )
          .toMinutes();

      if (durationSinceAccountLock >= 0 && durationSinceAccountLock < accountLockTimeout) {
        throw new RuntimeException(
            String.format("User account is locked... timeout duration in %s minutes",
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
