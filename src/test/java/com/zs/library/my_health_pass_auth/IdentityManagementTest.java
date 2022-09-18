package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.AppSecretUtil.PASSWORD_VALIDATION_RULES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import javax.persistence.PersistenceException;

import com.github.javafaker.Faker;
import com.zs.library.my_health_pass_auth.configuration.annotations.enable_postgres_test_container.PostgresTestContainer;
import com.zs.library.my_health_pass_auth.dto.UserAccountDetailsDto;
import com.zs.library.my_health_pass_auth.dto.UserIdentityDto;
import com.zs.library.my_health_pass_auth.entity.UserEntity;
import com.zs.library.my_health_pass_auth.pojo.FileDocument;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

  private UserAccountDetailsDto.UserAccountDetailsDtoBuilder accountDetailsBuilder;

  private String validPassword;


  @Autowired
  IdentityManagementTest(UserEntityRepository userRepository, IdentityManagement underTest) {
    this.userRepository = userRepository;
    this.underTest = underTest;
  }

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

    validPassword = PasswordGeneratorUtil.generateValidPassword(
        PASSWORD_VALIDATION_RULES
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

    String invalidPassword = PasswordGeneratorUtil.generateInValidPassword(
        PASSWORD_VALIDATION_RULES
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

  @Test
  @DisplayName("It should return user identity data if token is valid")
  void itShouldTokenIdentityData() {
    // Given
    UserAccountDetailsDto accountDetails = accountDetailsBuilder.build();

    // When
    long registeredUserId = underTest.register(accountDetails, validPassword);

    UserEntity user = userRepository.findById(registeredUserId)
        .orElse(null);

    String token = underTest.login(user.getUsername(), validPassword)
        .orElse(null);

    UserIdentityDto userIdentity = underTest.authenticate(token);

    // Then
    assertThat(userIdentity)
        .isNotNull()
        .satisfies(data -> {
          assertThat(data.getFirstName()).isEqualTo(user.getFirstName());
          assertThat(data.getLastName()).isEqualTo(user.getLastName());

          assertThat(data.getUsername()).isEqualTo(user.getUsername());
          assertThat(data.getId()).isEqualTo(user.getId());
        });
  }

  @Test
  @DisplayName("It should return empty profile picture if user does not exist")
  void itShouldReturnEmptyOptional() {
    // Given
    Long userId = faker.random().nextLong();

    // When
    Optional<FileDocument> document = underTest.getUserProfilePicture(userId);

    // Then
    assertThat(document).isNotPresent();
  }

  @Test
  @DisplayName("It should throw exception if file data not defined")
  void itShouldThrowExceptionIfFileDataIsNotDefined() {
    // Given
    String filename = faker.internet().slug();

    FileDocument document = FileDocument.builder()
        .filename(filename)
        .build();

    UserAccountDetailsDto accountDetails = accountDetailsBuilder
        .fileDocument(document)
        .build();

    // When
    Throwable throwable = catchThrowable(
        () -> underTest.register(accountDetails, validPassword)
    );

    // Then
    assertThat(throwable).isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("It should retrieve profile picture if defined")
  void itShouldRetrieveProfilePicture() {
    // Given
    String filename = faker.internet().slug();

    FileDocument document = FileDocument.builder()
        .bytes(filename.getBytes())
        .filename(filename)
        .build();

    UserAccountDetailsDto accountDetails = accountDetailsBuilder
        .fileDocument(document)
        .build();

    // When
    long registeredUserId = underTest.register(accountDetails, validPassword);

    Optional<FileDocument> userProfilePicture = underTest.getUserProfilePicture(
        registeredUserId
    );

    // Then
    assertThat(userProfilePicture).isPresent();

    assertThat(userProfilePicture.get()).satisfies(data -> {
      assertThat(data.getFilename()).isEqualTo(document.getFilename());
      assertThat(data.getBytes()).isEqualTo(document.getBytes());
    });
  }

}