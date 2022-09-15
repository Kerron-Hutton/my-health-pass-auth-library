package com.zs.library.my_health_pass_auth.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAccountDetailsDto {

  private LocalDate dateOfBirth;

  private String username;

  private String firstName;

  private String lastName;

}
