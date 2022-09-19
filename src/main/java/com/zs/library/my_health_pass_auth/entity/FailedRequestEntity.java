package com.zs.library.my_health_pass_auth.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.zs.library.my_health_pass_auth.enums.ApiRequestName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "failed_request", indexes = {
    @Index(
        columnList = "api_request, request_hash_code",
        name = "idx_failed_request_entity"
    )
})
public class FailedRequestEntity {
  @Id
  @SequenceGenerator(name = "failed_request_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "failed_request_seq")
  private Long id;

  @Column(name = "api_request", nullable = false)
  @Enumerated(EnumType.STRING)
  private ApiRequestName apiRequest;

  @Column(name = "request_hash_code", nullable = false)
  private int requestHashCode;

  @Column(name = "timestamp")
  private LocalDateTime timestamp;

  @Column(name = "is_request_blocked", columnDefinition = "boolean default false")
  private boolean isRequestBlocked;

  @Column(name = "request_block_timestamp")
  private LocalDateTime requestBlockTimestamp;

}
