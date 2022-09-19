package com.zs.library.my_health_pass_auth.dto;

import com.zs.library.my_health_pass_auth.enums.RegionCode;
import com.zs.library.my_health_pass_auth.pojo.FileDocument;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAccountDetailsDto {

  private FileDocument fileDocument;

  private LocalDate dateOfBirth;

  private RegionCode regionCode;

  private String username;

  private String firstName;

  private String lastName;

}
