package com.zs.library.my_health_pass_auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserIdentityDto {

  private String username;

  private String firstName;

  private String lastName;

  private long id;

}
