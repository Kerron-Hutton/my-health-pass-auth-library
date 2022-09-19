package com.zs.library.my_health_pass_auth;

import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.REQUEST_SIGNATURE_FAILURE_THRESHOLD;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.REQUEST_SIGNATURE_LOCK_DURATION;
import static com.zs.library.my_health_pass_auth.EnvironmentVariableKeys.REQUEST_SIGNATURE_MAX_FAILURE;

import com.zs.library.my_health_pass_auth.entity.FailedRequestEntity;
import com.zs.library.my_health_pass_auth.enums.ApiRequestName;
import com.zs.library.my_health_pass_auth.pojo.ApiRequestSignature;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.env.Environment;

@Slf4j
@RequiredArgsConstructor
class IdentityManagementHelper {

  private final FailedRequestRepository failedRequestRepository;

  private final Environment environment;

  public boolean handleFailedLoginRequest(ApiRequestSignature signature) {
    val failedRequests = failedRequestRepository.findAllByApiRequestAndHashCode(
        ApiRequestName.LOGIN, signature.hashCode()
    );

    if (failedRequests.isEmpty()) {
      addNewFailedAttempt(signature.hashCode());
      return true;
    }

    val failureThreshold = Integer.parseInt(
        environment.getRequiredProperty(REQUEST_SIGNATURE_FAILURE_THRESHOLD)
    );

    val requestLockDuration = Integer.parseInt(
        environment.getRequiredProperty(REQUEST_SIGNATURE_LOCK_DURATION)
    );

    val maxFailure = Integer.parseInt(
        environment.getRequiredProperty(REQUEST_SIGNATURE_MAX_FAILURE)
    );

    var failedAttemptCount = failedRequests.size();

    val lastFailedAttempt = failedRequests.get(failedAttemptCount - 1);
    val firstFailedAttempt = failedRequests.get(0);

    val durationBetweenAttempts = getDurationInMinutes(
        firstFailedAttempt.getTimestamp(), lastFailedAttempt.getTimestamp()
    );

    if (lastFailedAttempt.isRequestBlocked()) {
      val blockRequestDuration = getDurationInMinutes(
          lastFailedAttempt.getRequestBlockTimestamp(), LocalDateTime.now()
      );

      if (blockRequestDuration >= 0 && blockRequestDuration <= requestLockDuration) {
        log.info(
            String.format("Request is blocked... Timeout duration in %s minutes",
                requestLockDuration - durationBetweenAttempts
            )
        );

        return false;
      }

      failedRequestRepository.deleteAll(failedRequests);

      return true;
    }

    if (failedAttemptCount >= maxFailure && durationBetweenAttempts <= failureThreshold) {
      lastFailedAttempt.setRequestBlockTimestamp(LocalDateTime.now());
      lastFailedAttempt.setRequestBlocked(true);

      failedRequestRepository.save(lastFailedAttempt);

      log.info(
          String.format("Request is blocked... Timeout duration in %s minutes",
              requestLockDuration - durationBetweenAttempts
          )
      );

      return false;
    }

    addNewFailedAttempt(signature.hashCode());

    return true;
  }

  private void addNewFailedAttempt(int hashCode) {
    val newRequest = new FailedRequestEntity();

    newRequest.setApiRequest(ApiRequestName.LOGIN);
    newRequest.setRequestHashCode(hashCode);

    newRequest.setTimestamp(LocalDateTime.now());

    failedRequestRepository.save(newRequest);
  }

  public long getDurationInMinutes(LocalDateTime start, LocalDateTime end) {
    return Duration.between(start, end).toMinutes();
  }

}
