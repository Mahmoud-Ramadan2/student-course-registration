package com.mahmoudramadan.studentregistration.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    private final JwtProperties jwtProperties;


    public String generateAccessToken(UserDetails userDetails) {
        log.debug("Generating access token for username={}", userDetails.getUsername());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpirationMs()))
                .signWith( getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        String username = parseClaims(token).getSubject();
        log.debug("Extracted username={} from token", username);
        return username;
    }

    public boolean isValid(String token, UserDetails user) {
        boolean valid = extractUsername(token).equals(user.getUsername())
                && !isExpired(token)
                && user.isEnabled();
        if (!valid) {
            log.warn("Token validation failed for username={}", user.getUsername());
        }
        return valid;
    }
    public long getExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationMs() / 1000;
    }
    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
        private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
