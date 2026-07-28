package com.mahmoudramadan.studentregistration.auth.entity;

import com.mahmoudramadan.studentregistration.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "jti")
    private String jti;

    @Column(name = "family_id")
    private String familyId;

    @Column(name = "parent_jti")
    private String parentJti;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "ip_address", columnDefinition = "INET")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_jti")
    private String replacedByJti;

    @Column(name = "reuse_detected")
    private boolean reuseDetected;

}

