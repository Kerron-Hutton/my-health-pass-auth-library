package com.zs.library.my_health_pass_auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.github.javafaker.Faker;
import com.zs.library.my_health_pass_auth.entity.RegionEntity;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import com.zs.library.my_health_pass_auth.pojo.ApiRequestSignature;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Login Request Test")
class LoginRequestTest {

  private final Faker faker = new Faker();

  private IdentityManagementHelper identityManagementHelper;

  private UserEntityRepository userRepository;

  private ApiRequestSignature testSignature;

  private IdentityManagement underTest;

  private JwtTokenUtil jwtTokenUtil;

  @BeforeEach
  void setup() {
    identityManagementHelper = Mockito.mock(IdentityManagementHelper.class);

    RegionRepository regionRepository = Mockito.mock(RegionRepository.class);
    FileServerUtil fileServerUtil = Mockito.mock(FileServerUtil.class);

    userRepository = Mockito.mock(UserEntityRepository.class);
    jwtTokenUtil = Mockito.mock(JwtTokenUtil.class);

    underTest = new IdentityManagement(
        identityManagementHelper, regionRepository, userRepository,
        fileServerUtil, jwtTokenUtil
    );

    testSignature = ApiRequestSignature.builder()
        .userAgent(faker.internet().userAgentAny())
        .clientIp(faker.internet().ipV4Address())
        .build();
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

    Optional<String> token = underTest.login(username, password, testSignature);

    // Then
    assertThat(token).isNotPresent();
  }

  @Test
  @DisplayName("It should throw an exception if lock timestamp is less than lock duration")
  void itShouldThrowException() {
    // Given
    String username = faker.internet().emailAddress();
    String password = faker.internet().password();

    RegionEntity region = new RegionEntity();
    region.setAccountLockDuration(2);

    UserEntity userByUsername = new UserEntity();
    userByUsername.setAccountLockTimestamp(LocalDateTime.now().minusMinutes(1));
    userByUsername.setAccountLocked(true);
    userByUsername.setRegion(region);

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(identityManagementHelper.handleFailedLoginRequest(Mockito.any()))
        .thenReturn(true);

    Throwable throwable = catchThrowable(
        () -> underTest.login(username, password, testSignature)
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

    RegionEntity region = new RegionEntity();
    region.setAccountLockDuration(2);
    region.setMaxFailedLogin(2);

    UserEntity userByUsername = Mockito.spy(UserEntity.class);
    userByUsername.setAccountLockTimestamp(LocalDateTime.now().plusHours(1));
    userByUsername.setAccountLocked(true);
    userByUsername.setUsername(username);
    userByUsername.setRegion(region);

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(identityManagementHelper.handleFailedLoginRequest(Mockito.any()))
        .thenReturn(true);

    Optional<String> token = underTest.login(username, password, testSignature);

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

    RegionEntity region = new RegionEntity();
    region.setAccountLockDuration(10);
    region.setMaxFailedLogin(2);

    UserEntity userByUsername = Mockito.spy(UserEntity.class);
    userByUsername.setFailedLogins(region.getMaxFailedLogin());
    userByUsername.setRegion(region);

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(identityManagementHelper.handleFailedLoginRequest(Mockito.any()))
        .thenReturn(true);

    Optional<String> token = underTest.login(username, password, testSignature);

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

    RegionEntity region = new RegionEntity();
    region.setAccountLockDuration(10);

    UserEntity userByUsername = Mockito.spy(UserEntity.class);
    userByUsername.setUsername(username);
    userByUsername.setRegion(region);
    userByUsername.setId(1L);

    userByUsername.setPassword(
        UserPasswordUtil.generatePasswordHash(password)
    );

    // When
    Mockito
        .when(userRepository.findByUsername(Mockito.anyString()))
        .thenReturn(Optional.of(userByUsername));

    Mockito
        .when(jwtTokenUtil.generateUserAuthToken(Mockito.any(), Mockito.any()))
        .thenReturn(sampleToken);

    Optional<String> token = underTest.login(username, password, testSignature);

    // Then
    assertThat(token).isPresent();

    assertThat(token.get()).contains(sampleToken);

    Mockito.verify(userByUsername).setAccountLockTimestamp(null);

    Mockito.verify(userByUsername).setAccountLocked(false);

    Mockito.verify(userByUsername).setFailedLogins(0);
  }

}