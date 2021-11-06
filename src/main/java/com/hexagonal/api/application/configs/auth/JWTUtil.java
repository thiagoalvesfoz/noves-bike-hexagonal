package com.hexagonal.api.application.configs.auth;

import com.hexagonal.api.application.adapters.security.model.UserSecurity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class JWTUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  public String generateToken(UserSecurity user) {
    var date = new Date(System.currentTimeMillis());

    return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("user", buildUser(user))
            .setIssuedAt(date)
            .setExpiration(new Date(date.getTime() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret.getBytes())
            .compact();
  }

  private HashMap<String, Object> buildUser(UserSecurity user) {
    var hash = new HashMap<String, Object>();
    hash.put("id", user.getId());
    hash.put("name", user.getName());
    hash.put("avatar", user.getAvatar());

    return hash;
  }

  public boolean isValidToken(String token) {

    Claims claims = getClaims(token);

    if (claims != null) {

      var username = claims.getSubject();
      var expirationDate = claims.getExpiration();
      var now = new Date(System.currentTimeMillis());

      return username != null && expirationDate != null && now.before(expirationDate);
    }

    return false;
  }

  private Claims getClaims(String token) {
    try {
      return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
    } catch (Exception ex) {
      return null;
    }
  }

  public String getUsername(String token) {

    var claims = getClaims(token);

    if (claims != null) {
      return claims.getSubject();
    }

    return null;
  }
}