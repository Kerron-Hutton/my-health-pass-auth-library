package com.zs.library.my_health_pass_auth.pojo;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class ApiRequestSignature {

  private Map<String, String> cookies;

  private String userAgent;

  private String clientIp;

}
