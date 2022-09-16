package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.AppSecretUtil.JWT_SECRET_VALIDATION_RULES;
import static com.zs.library.my_health_pass_auth.JwtTokenUtil.AUTHENTICATION_JWT_SECRET_PROPERTY;
import static com.zs.library.my_health_pass_auth.JwtTokenUtil.AUTHENTICATION_TOKEN_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.github.javafaker.Faker;
import com.zs.library.my_health_pass_auth.dto.UserIdentityDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

@DisplayName("JWT Token Util Test")
class JwtTokenUtilTest {

  private final Faker faker = new Faker();
  private UserIdentityDto userIdentityInput;
  private Environment environment;

  private JwtTokenUtil underTest;

  @BeforeEach
  void setup() {
    userIdentityInput = UserIdentityDto.builder()
        .username(faker.internet().emailAddress())
        .firstName(faker.name().firstName())
        .lastName(faker.name().lastName())
        .id(faker.random().nextLong())
        .build();

    environment = Mockito.mock(Environment.class);

    underTest = new JwtTokenUtil(environment);
  }

  @Test
  @DisplayName("It should return token if jwt secret is valid")
  void itShouldReturnTokenIfJwtSecretIsValid() {
    // Given
    String secret = PasswordGeneratorUtil.generateValidPassword(
        JWT_SECRET_VALIDATION_RULES
    );

    // When
    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_JWT_SECRET_PROPERTY))
        .thenReturn(secret);

    String token = underTest.generateUserAuthToken(userIdentityInput);

    // Then
    assertThat(token).startsWith(AUTHENTICATION_TOKEN_PREFIX);
  }

  @Test
  @DisplayName("It should throw exception if jwt secret is not valid")
  void itShouldThrowExceptionIfJwtSecretIsNotValid() {
    // Given
    String secret = PasswordGeneratorUtil.generateInValidPassword(
        JWT_SECRET_VALIDATION_RULES
    );

    // When
    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_JWT_SECRET_PROPERTY))
        .thenReturn(secret);

    Throwable throwable = catchThrowable(
        () -> underTest.generateUserAuthToken(userIdentityInput)
    );

    // Then
    assertThat(throwable).isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("It should return user data from token if valid")
  void itShouldReturnUserDataIfTokenIsValid() {
    // Given
    String secret = PasswordGeneratorUtil.generateValidPassword(
        JWT_SECRET_VALIDATION_RULES
    );

    // When
    Mockito
        .when(environment.getRequiredProperty(AUTHENTICATION_JWT_SECRET_PROPERTY))
        .thenReturn(secret);

    String token = underTest.generateUserAuthToken(userIdentityInput);

    UserIdentityDto userData = underTest.getUserIdentityIfTokenIsValid(token);

    // Then
    assertThat(userData).satisfies(data -> {
      assertThat(data.getFirstName()).isEqualTo(userIdentityInput.getFirstName());
      assertThat(data.getLastName()).isEqualTo(userIdentityInput.getLastName());

      assertThat(data.getUsername()).isEqualTo(userIdentityInput.getUsername());
      assertThat(data.getId()).isEqualTo(userIdentityInput.getId());
    });
  }
}