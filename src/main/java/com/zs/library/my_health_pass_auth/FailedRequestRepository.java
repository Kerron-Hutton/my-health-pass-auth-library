package com.zs.library.my_health_pass_auth;

import com.zs.library.my_health_pass_auth.entity.FailedRequestEntity;
import com.zs.library.my_health_pass_auth.enums.ApiRequestName;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

interface FailedRequestRepository extends CrudRepository<FailedRequestEntity, Long> {

  @Query("SELECT f FROM FailedRequestEntity f WHERE f.apiRequest = :request AND f.requestHashCode = :hashCode")
  List<FailedRequestEntity> findAllByApiRequestAndHashCode(ApiRequestName request, int hashCode);

}
