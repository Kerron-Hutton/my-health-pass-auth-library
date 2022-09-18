package com.zs.library.my_health_pass_auth.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.zs.library.my_health_pass_auth.dto.UserAccountDetailsDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "\"user\"")
public class UserEntity {
  @Id
  @SequenceGenerator(name = "user_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
  private Long id;

  @Column(name = "username", nullable = false, length = 80, unique = true)
  private String username;

  @Column(name = "first_name", nullable = false, length = 80)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 80)
  private String lastName;

  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  @Column(name = "profile_picture", length = 120)
  private String profilePicture;

  @Column(name = "password", nullable = false, length = 64)
  private String password;

  @Column(name = "failed_logins")
  private int failedLogins;

  @Column(name = "is_account_locked", columnDefinition = "boolean default false")
  private boolean isAccountLocked;

  @Column(name = "account_lock_timestamp")
  private LocalDateTime accountLockTimestamp;

  public UserEntity(UserAccountDetailsDto accountDetails, String passwordHash) {
    this.firstName = accountDetails.getFirstName();
    this.lastName = accountDetails.getLastName();

    this.dateOfBirth = accountDetails.getDateOfBirth();
    this.username = accountDetails.getUsername();
    this.password = passwordHash;

    if (accountDetails.getFileDocument() != null) {
      this.profilePicture = accountDetails.getFileDocument().getFilename();
    }
  }

}
