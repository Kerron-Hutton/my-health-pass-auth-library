package com.zs.library.my_health_pass_auth;

import com.zs.library.my_health_pass_auth.entity.FailedRequestEntity;
import org.springframework.data.repository.CrudRepository;

interface FailedRequestRepository extends CrudRepository<FailedRequestEntity, Long> {
}
