package com.zs.library.my_health_pass_auth;

import javax.crypto.SecretKey;

import com.zs.library.my_health_pass_auth.dto.UserIdentityDto;
import com.zs.library.my_health_pass_auth.entity.RegionEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
class JwtTokenUtil {

  public static final String AUTHENTICATION_JWT_SECRET_PROPERTY = "authentication.jwt.secret";

  public static final String AUTHENTICATION_TOKEN_PREFIX = "Bearer";

  public static final String TOKEN_PAYLOAD_ATTRIBUTE = "payload";

  private final Environment environment;

  public String generateUserAuthToken(UserIdentityDto userIdentity, RegionEntity region) {
    val sessionDuration = getTokenExpirationDateTime(region);

    val token = Jwts.builder()
        .claim(TOKEN_PAYLOAD_ATTRIBUTE, userIdentity)
        .setIssuedAt(getTokenIssueAtDateTime())
        .setSubject(userIdentity.getUsername())
        .setExpiration(sessionDuration)
        .signWith(getSecretKey())
        .compact();

    return String.format("%s %s", AUTHENTICATION_TOKEN_PREFIX, token);
  }

  public UserIdentityDto getUserIdentityIfTokenIsValid(String token) {
    val jwtToken = token.replace(AUTHENTICATION_TOKEN_PREFIX, "").trim();

    val jwsClaims = Jwts.parserBuilder()
        .setSigningKey(getSecretKey()).build()
        .parseClaimsJws(jwtToken);

    val payload = (Map<?, ?>) jwsClaims.getBody().get(
        TOKEN_PAYLOAD_ATTRIBUTE
    );

    return UserIdentityDto.builder()
        .id(Long.parseLong(payload.get("id").toString()))
        .firstName((String) payload.get("firstName"))
        .lastName((String) payload.get("lastName"))
        .username((String) payload.get("username"))
        .build();
  }

  private SecretKey getSecretKey() {
    val jwtSecret = environment.getRequiredProperty(AUTHENTICATION_JWT_SECRET_PROPERTY);

    val validationMessage = AppSecretUtil.validateSecretAgainstRules(
        jwtSecret, AppSecretUtil.JWT_SECRET_VALIDATION_RULES
    );

    if (validationMessage.isPresent()) {
      throw new RuntimeException(validationMessage.get());
    }

    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }

  private Date getTokenExpirationDateTime(RegionEntity region) {
    return java.sql.Timestamp.valueOf(
        LocalDateTime.now().plusMinutes(region.getSessionDuration())
    );
  }

  private Date getTokenIssueAtDateTime() {
    return java.sql.Timestamp.valueOf(LocalDateTime.now());
  }

}
