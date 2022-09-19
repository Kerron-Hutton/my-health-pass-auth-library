package com.zs.library.my_health_pass_auth.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.zs.library.my_health_pass_auth.enums.RegionCode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "region")
public class RegionEntity {

  @Id
  @Column(name = "id", nullable = false)
  @SequenceGenerator(name = "region_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_seq")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "code", nullable = false)
  @Enumerated(EnumType.STRING)
  private RegionCode code;

  @Column(name = "session_duration", nullable = false)
  private int sessionDuration;

  @Column(name = "account_lock_duration", nullable = false)
  private int accountLockDuration;

  @Column(name = "max_failed_login", nullable = false)
  private int maxFailedLogin;

  @Column(name = "min_password_length", nullable = false)
  private int minPasswordLength;

  @Column(name = "max_password_length", nullable = false)
  private int maxPasswordLength;

  @Column(name = "include_digit", columnDefinition = "boolean default false")
  private boolean includeDigit;

  @Column(name = "include_special_character", columnDefinition = "boolean default false")
  private boolean includeSpecialCharacter;

  @OneToMany(mappedBy = "region")
  private List<UserEntity> users = new ArrayList<>();

}
