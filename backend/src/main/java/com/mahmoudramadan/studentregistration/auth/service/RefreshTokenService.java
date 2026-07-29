package com.mahmoudramadan.studentregistration.auth.service;

import com.mahmoudramadan.studentregistration.auth.entity.RefreshToken;
import com.mahmoudramadan.studentregistration.auth.repo.RefreshTokenRepository;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.infra.security.JwtProperties;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;


    @Transactional
    public String createRefreshToken(CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        String rawToken = generateRawToken();
        String jti = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .jti(jti)
                .familyId(familyId)
                .ipAddress(getClientIp())
                .userAgent(getUserAgent())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();

        refreshTokenRepository.save(token);
        return rawToken;
    }

    @Transactional
    public String rotateRefreshToken(RefreshToken current) {
        current.setRevokedAt(Instant.now());
        current.setReplacedByJti(UUID.randomUUID().toString());
        refreshTokenRepository.save(current);

        String newRawToken = generateRawToken();

        RefreshToken rotated = RefreshToken.builder()
                .user(current.getUser())
                .tokenHash(hashToken(newRawToken))
                .jti(current.getReplacedByJti())
                .familyId(current.getFamilyId())
                .parentJti(current.getJti())
                .ipAddress(getClientIp())
                .userAgent(getUserAgent())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();

        refreshTokenRepository.save(rotated);
        return newRawToken;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {
        String hash = hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired");
        }
        if (token.getRevokedAt() != null) {
            if (!token.isReuseDetected()) {
                token.setReuseDetected(true);
                refreshTokenRepository.save(token);
                revokeFamily(token.getFamilyId());
            }
            throw new BusinessException("Refresh token revoked");
        }
        return token;
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateRawToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private InetAddress getClientIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return InetAddress.getByName(ip != null ? ip : "0.0.0.0");
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserAgent() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }

    private void revokeFamily(String familyId) {
        refreshTokenRepository.findAllByFamilyId(familyId)
                .forEach(token -> {
                    token.setRevokedAt(Instant.now());
                    token.setReuseDetected(true);
                    refreshTokenRepository.save(token);
                });
    }
}
