package com.zs.library.my_health_pass_auth;

import com.zs.library.my_health_pass_auth.entity.RegionEntity;
import com.zs.library.my_health_pass_auth.enums.RegionCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

interface RegionRepository extends CrudRepository<RegionEntity, Long> {

  @Query("SELECT r FROM RegionEntity r WHERE r.code = :code")
  RegionEntity findByRegionCode(RegionCode code);

}
