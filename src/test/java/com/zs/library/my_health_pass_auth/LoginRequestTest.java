package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.AUTHENTICATION_ACCOUNT_LOCK_DURATION;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.AUTHENTICATION_MAX_LOGIN_ATTEMPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.github.javafaker.Faker;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

@DisplayName("Login Request Test")
class LoginRequestTest {

  private final Faker faker = new Faker();

  private UserEntityRepository userRepository;

  private IdentityManagement underTest;

  private JwtTokenUtil jwtTokenUtil;

  private Environment environment;


  @BeforeEach
  void setup() {
    userRepository = Mockito.mock(UserEntityRepository.class);
    jwtTokenUtil = Mockito.mock(JwtTokenUtil.class);
    environment = Mockito.mock(Environment.class);

    underTest = new IdentityManagement(
        userRepository, jwtTokenUtil, environment
    );
  }

  @Test
  @DisplayName("It should return empty optional if username not found")
  void itShouldReturnNull() {
    // Given
    String username = faker.internet().emailAddress();
    String password = faker.internet().password();

    // When
    Mockito
        .when(userRepository.findByUsername(username))
        .thenReturn(Optional.empty());

    Optional<String> token = underTest.login(username, password);

    // Then
    assertThat(token).isNotPresent();
  }

  @Test
  @DisplayName("It should throw an exception if lock timestamp is less than lock duration")
  void itShouldThrowException() {
    // Given
    String username = faker.internet().emailAddress();
    String password = faker.internet().password();

    UserEntity userByUsername = new UserEntity();
    userByUsername.setAccountLockTimestamp(LocalDateTime.now().minusMinutes(1));
    userByUsername.setAccountLocked(true);

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_ACCOUNT_LOCK_DURATION))
        .thenReturn("2");

    Throwable throwable = catchThrowable(
        () -> underTest.login(username, password)
    );

    // Then
    assertThat(throwable).isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("It should reset account after timeout duration")
  void itShouldResetUserAfterAccountLock() {
    // Given
    String username = faker.internet().emailAddress();
    String password = faker.internet().password();

    UserEntity userByUsername = Mockito.spy(UserEntity.class);
    userByUsername.setAccountLockTimestamp(LocalDateTime.now().plusHours(1));
    userByUsername.setAccountLocked(true);

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_ACCOUNT_LOCK_DURATION))
        .thenReturn("2");

    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_MAX_LOGIN_ATTEMPT))
        .thenReturn("2");

    Optional<String> token = underTest.login(username, password);

    // Then
    assertThat(token).isNotPresent();

    Mockito.verify(userByUsername).setAccountLockTimestamp(null);

    Mockito.verify(userByUsername).setAccountLocked(false);

    Mockito.verify(userByUsername).setFailedLogins(0);
  }

  @Test
  @DisplayName("It should lock account if failed login attempt is equal to max")
  void itShouldLockAccount() {
    // Given
    String username = faker.internet().emailAddress();
    String password = faker.internet().password();

    int failedLoginAttempts = 2;

    UserEntity userByUsername = Mockito.spy(UserEntity.class);
    userByUsername.setFailedLogins(failedLoginAttempts);

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_ACCOUNT_LOCK_DURATION))
        .thenReturn("10");

    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_MAX_LOGIN_ATTEMPT))
        .thenReturn(String.valueOf(failedLoginAttempts));

    Optional<String> token = underTest.login(username, password);

    // Then
    assertThat(token).isNotPresent();

    assertThat(userByUsername.getAccountLockTimestamp()).isNotNull();

    Mockito.verify(userByUsername).setAccountLocked(true);
  }

  @Test
  @DisplayName("It should login user if credentials are correct")
  void itShouldLoginUserIfTokenIsValid() {
    // Given
    String username = faker.internet().emailAddress();
    String password = faker.internet().password();
    String sampleToken = faker.internet().slug();

    UserEntity userByUsername = Mockito.spy(UserEntity.class);
    userByUsername.setUsername(username);
    userByUsername.setId(1L);

    userByUsername.setPassword(
        UserPasswordUtil.generatePasswordHash(password)
    );

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_ACCOUNT_LOCK_DURATION))
        .thenReturn("10");

    Mockito
        .when(jwtTokenUtil.generateUserAuthToken(Mockito.any()))
        .thenReturn(sampleToken);

    Optional<String> token = underTest.login(username, password);

    // Then
    assertThat(token).isPresent();

    assertThat(token.get()).contains(sampleToken);

    Mockito.verify(userByUsername).setAccountLockTimestamp(null);

    Mockito.verify(userByUsername).setAccountLocked(false);

    Mockito.verify(userByUsername).setFailedLogins(0);
  }

}