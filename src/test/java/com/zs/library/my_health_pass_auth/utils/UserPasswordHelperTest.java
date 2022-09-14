package com.zs.library.my_health_pass_auth.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Password Helper Test")
class UserPasswordHelperTest {

  private Faker faker;

  @BeforeEach
  void setup() {
    faker = new Faker();
  }

  @Test
  @DisplayName("It should generate a string with 64 characters")
  void itShouldGenerateAStringWith64Characters() {
    // Given
    String password = faker.internet().password();

    // When
    String passwordHash = UserPasswordHelper.generatePasswordHash(password);

    // Then
    assertThat(passwordHash).hasSize(64);
  }

  @Test
  @DisplayName("It should always generate the same hash for the same input")
  void itShouldAlwaysGenerateTheSameHash() {
    // Given
    String password = faker.internet().password();

    //When
    String passwordHashOne = UserPasswordHelper.generatePasswordHash(password);
    String passwordHashTwo = UserPasswordHelper.generatePasswordHash(password);

    // Then
    assertThat(passwordHashOne).isEqualTo(passwordHashTwo);
  }

  @Test
  @DisplayName("It should always generate different hash for the different input")
  void itShouldAlwaysGenerateTheDifferentHash() {
    // Given
    String passwordOne = faker.internet().password();
    String passwordTwo = faker.internet().password();

    //When
    String passwordHashOne = UserPasswordHelper.generatePasswordHash(passwordOne);
    String passwordHashTwo = UserPasswordHelper.generatePasswordHash(passwordTwo);

    // Then
    assertThat(passwordHashOne).isNotEqualTo(passwordHashTwo);
  }

  @Test
  @DisplayName("It should fail password validation if password hash does not match")
  void itShouldFailPasswordValidation() {
    // Given
    String password = faker.internet().password();

    String passwordHash = faker.crypto().sha256();

    // When
    Boolean isValid = UserPasswordHelper.validate(password, passwordHash);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("It should pass password validation if password hash match")
  void itShouldPassPasswordValidation() {
    // Given
    String password = faker.internet().password();

    String passwordHash = UserPasswordHelper.generatePasswordHash(password);

    // When
    Boolean isValid = UserPasswordHelper.validate(password, passwordHash);

    // Then
    assertThat(isValid).isTrue();
  }

}
