package com.zs.library.my_health_pass_auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import javax.persistence.PersistenceException;

import com.github.javafaker.Faker;
import com.zs.library.my_health_pass_auth.configuration.annotations.enable_postgres_test_container.PostgresTestContainer;
import com.zs.library.my_health_pass_auth.dto.UserAccountDetailsDto;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@PostgresTestContainer
@ActiveProfiles("test")
@DisplayName("Identity Management Test")
class IdentityManagementTest {

  private final UserEntityRepository userRepository;

  private final IdentityManagement underTest;

  private final Faker faker = new Faker();

  @Autowired
  IdentityManagementTest(UserEntityRepository userRepository, IdentityManagement underTest) {
    this.userRepository = userRepository;
    this.underTest = underTest;
  }

  @Nested
  @PostgresTestContainer
  @DisplayName("User Registration Test")
  class UserRegistrationTest {

    private UserAccountDetailsDto.UserAccountDetailsDtoBuilder accountDetailsBuilder;

    private String validPassword;

    @BeforeEach
    void setup() {
      LocalDate dateOfBirth = faker.date().birthday().toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDate();

      accountDetailsBuilder = UserAccountDetailsDto.builder()
          .username(faker.internet().emailAddress())
          .firstName(faker.name().firstName())
          .lastName(faker.name().lastName())
          .dateOfBirth(dateOfBirth);

      validPassword = faker.internet().password(
          UserPasswordUtil.PASSWORD_MIN_CONSTRAINT,
          UserPasswordUtil.PASSWORD_MAX_CONSTRAINT,
          true,
          true,
          true
      );
    }

    @Test
    @DisplayName("It should successfully register new user")
    void itShouldRegisterUser() {
      // Given
      UserAccountDetailsDto accountDetails = accountDetailsBuilder.build();

      // When
      long registeredUserId = underTest.register(accountDetails, validPassword);

      Optional<UserEntity> user = userRepository.findById(registeredUserId);

      // Then
      assertThat(user).isPresent();

      assertThat(user.get()).satisfies(userEntity -> {
        assertThat(userEntity.getUsername()).isEqualTo(accountDetails.getUsername());
        assertThat(userEntity.getFirstName()).isEqualTo(accountDetails.getFirstName());

        assertThat(userEntity.getDateOfBirth()).isEqualTo(accountDetails.getDateOfBirth());
        assertThat(userEntity.getLastName()).isEqualTo(accountDetails.getLastName());

        assertThat(userEntity.getPassword()).hasSize(64);
      });
    }

    @Test
    @DisplayName("It should throw exception if user password is not valid")
    void itShouldThrowExceptionIfPasswordIsNotValid() {
      // Given
      UserAccountDetailsDto accountDetails = accountDetailsBuilder.build();

      String invalidPassword = faker.internet().password(
          1, UserPasswordUtil.PASSWORD_MIN_CONSTRAINT - 1
      );

      // When
      Throwable throwable = catchThrowable(
          () -> underTest.register(accountDetails, invalidPassword)
      );

      // Then
      assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("It should throw exception if date of birth is in the future")
    void itShouldThrowExceptionIfDateOfBirthIsNotValid() {
      // Given
      LocalDate futureDateOfBirth = LocalDate.now().plusDays(1);

      UserAccountDetailsDto accountDetails = accountDetailsBuilder
          .dateOfBirth(futureDateOfBirth)
          .build();

      // When
      Throwable throwable = catchThrowable(
          () -> underTest.register(accountDetails, validPassword)
      );

      // Then
      assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("It should throw exception if any account detail information is null")
    void itShouldThrowExceptionIfAccountDetailIsNotValid() {
      // Given
      UserAccountDetailsDto accountDetails = accountDetailsBuilder
          .username(null)
          .build();

      // When
      Throwable throwable = catchThrowable(
          () -> underTest.register(accountDetails, validPassword)
      );

      // Then
      assertThat(throwable).isInstanceOf(PersistenceException.class);
    }

  }

}