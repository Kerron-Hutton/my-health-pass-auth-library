package com.zs.library.my_health_pass_auth.pojo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDocument {

  private String filename;

  @Builder.Default
  private byte[] bytes = {};

}
