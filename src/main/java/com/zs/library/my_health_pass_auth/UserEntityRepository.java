package com.zs.library.my_health_pass_auth;

import com.zs.library.my_health_pass_auth.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

interface UserEntityRepository extends CrudRepository<UserEntity, Long> {

  @Query("SELECT u FROM UserEntity u WHERE upper(u.username) = upper(:username)")
  Optional<UserEntity> findByUsername(String username);

}
