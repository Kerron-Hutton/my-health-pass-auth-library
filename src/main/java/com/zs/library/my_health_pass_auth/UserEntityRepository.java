package com.zs.library.my_health_pass_auth;

import com.zs.library.my_health_pass_auth.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

interface UserEntityRepository extends CrudRepository<UserEntity, Long> {
}
