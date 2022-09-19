package com.zs.library.my_health_pass_auth;

final class EnvironmentVariableKeys {

  public static final String AUTHENTICATION_ACCOUNT_LOCK_DURATION = "authentication.account_lock.duration";

  public static final String AUTHENTICATION_MAX_LOGIN_ATTEMPT = "authentication.max.login.attempt";

  public static final String FILE_SERVER_DIRECTORY = "file.server.directory";

  public static final String REQUEST_SIGNATURE_FAILURE_THRESHOLD = "request.signature_failure.threshold";

  public static final String REQUEST_SIGNATURE_LOCK_DURATION = "request.signature_lock.duration";

  public static final String REQUEST_SIGNATURE_MAX_FAILURE = "request.signature_max.failure";

  private EnvironmentVariableKeys() {
  }

}
