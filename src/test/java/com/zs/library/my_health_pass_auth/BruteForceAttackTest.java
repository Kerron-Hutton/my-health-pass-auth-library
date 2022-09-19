package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.REQUEST_SIGNATURE_FAILURE_THRESHOLD;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.REQUEST_SIGNATURE_LOCK_DURATION;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.REQUEST_SIGNATURE_MAX_FAILURE;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.javafaker.Faker;
import com.zs.library.my_health_pass_auth.entity.FailedRequestEntity;
import com.zs.library.my_health_pass_auth.enums.ApiRequestName;
import com.zs.library.my_health_pass_auth.pojo.ApiRequestSignature;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

@DisplayName("Brute Force Attack Test")
class BruteForceAttackTest {

  private final Faker faker = new Faker();

  private FailedRequestRepository failedRequestRepository;

  private IdentityManagementHelper underTest;

  private ApiRequestSignature testSignature;

  private Environment environment;

  @BeforeEach
  void setup() {
    failedRequestRepository = Mockito.mock(FailedRequestRepository.class);
    environment = Mockito.mock(Environment.class);

    underTest = new IdentityManagementHelper(failedRequestRepository, environment);

    testSignature = ApiRequestSignature.builder()
        .userAgent(faker.internet().userAgentAny())
        .clientIp(faker.internet().ipV4Address())
        .build();
  }

  @Test
  @DisplayName("It should return false if request is blocked")
  void itShouldReturnFalseIfRequestIsStillBlocked() {
    // Given
    FailedRequestEntity lastFailedRequest = new FailedRequestEntity();

    lastFailedRequest.setRequestBlockTimestamp(LocalDateTime.now());
    lastFailedRequest.setTimestamp(LocalDateTime.now());
    lastFailedRequest.setRequestBlocked(true);

    // When
    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_FAILURE_THRESHOLD))
        .thenReturn("10");

    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_LOCK_DURATION))
        .thenReturn("30");

    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_MAX_FAILURE))
        .thenReturn("1");

    Mockito
        .when(failedRequestRepository.findAllByApiRequestAndHashCode(
            ApiRequestName.LOGIN, testSignature.hashCode()
        ))
        .thenReturn(List.of(lastFailedRequest));

    Boolean shouldContinue = underTest.handleFailedLoginRequest(testSignature);

    // Then
    assertThat(shouldContinue).isFalse();
  }

  @Test
  @DisplayName("It should return true and reset blocked request")
  void itShouldReturnTrueAndResetBlockedRequest() {
    // Given
    FailedRequestEntity lastFailedRequest = new FailedRequestEntity();

    lastFailedRequest.setRequestBlockTimestamp(LocalDateTime.now().plusMinutes(35));
    lastFailedRequest.setTimestamp(LocalDateTime.now());
    lastFailedRequest.setRequestBlocked(true);

    // When
    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_FAILURE_THRESHOLD))
        .thenReturn("10");

    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_LOCK_DURATION))
        .thenReturn("30");

    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_MAX_FAILURE))
        .thenReturn("1");

    Mockito
        .when(failedRequestRepository.findAllByApiRequestAndHashCode(
            ApiRequestName.LOGIN, testSignature.hashCode()
        ))
        .thenReturn(List.of(lastFailedRequest));

    Boolean shouldContinue = underTest.handleFailedLoginRequest(testSignature);

    // Then
    assertThat(shouldContinue).isTrue();

    Mockito.verify(failedRequestRepository).deleteAll(
        Mockito.any()
    );
  }

  @Test
  @DisplayName("It should return false and blocked request")
  void itShouldReturnFalseAndBlockRequest() {
    // Given
    FailedRequestEntity lastFailedRequest = new FailedRequestEntity();

    lastFailedRequest.setTimestamp(LocalDateTime.now());

    // When
    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_FAILURE_THRESHOLD))
        .thenReturn("10");

    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_LOCK_DURATION))
        .thenReturn("30");

    Mockito
        .when(environment.getRequiredProperty(REQUEST_SIGNATURE_MAX_FAILURE))
        .thenReturn("1");

    Mockito
        .when(failedRequestRepository.findAllByApiRequestAndHashCode(
            ApiRequestName.LOGIN, testSignature.hashCode()
        ))
        .thenReturn(List.of(lastFailedRequest));

    Boolean shouldContinue = underTest.handleFailedLoginRequest(testSignature);

    lastFailedRequest.setRequestBlocked(true);

    // Then
    assertThat(shouldContinue).isFalse();

    Mockito.verify(failedRequestRepository).save(
        lastFailedRequest
    );
  }

}
