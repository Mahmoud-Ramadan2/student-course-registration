package com.mahmoudramadan.studentregistration.auth.service;

import com.mahmoudramadan.studentregistration.auth.dto.AuthResponse;
import com.mahmoudramadan.studentregistration.auth.dto.LoginRequest;
import com.mahmoudramadan.studentregistration.auth.dto.RefreshTokenRequest;
import com.mahmoudramadan.studentregistration.auth.entity.RefreshToken;
import com.mahmoudramadan.studentregistration.enrollment.event.WaitlistPromotedEvent;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.infra.security.JwtTokenService;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt username={}", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Login successful username={}", request.username());

        userRepository.findById(userDetails.getId()).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });

        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails);

        return new AuthResponse(accessToken,  jwtTokenService.getExpirationSeconds(), refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.debug("Token refresh request");
        RefreshToken current = refreshTokenService.validateRefreshToken(request.refreshToken());
        String newRefreshToken = refreshTokenService.rotateRefreshToken(current);
        CustomUserDetails userDetails = new CustomUserDetails(current.getUser());
        String accessToken = jwtTokenService.generateAccessToken(userDetails);

        return new AuthResponse(accessToken,  jwtTokenService.getExpirationSeconds(), newRefreshToken);

    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        log.debug("Logout request");
        refreshTokenService.revokeRefreshToken(request.refreshToken());
    }
}
