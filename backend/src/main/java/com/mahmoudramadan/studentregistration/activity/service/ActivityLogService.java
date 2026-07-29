package com.mahmoudramadan.studentregistration.activity.service;

import com.mahmoudramadan.studentregistration.activity.dto.ActivityLogResponse;
import com.mahmoudramadan.studentregistration.activity.entity.ActivityLog;
import com.mahmoudramadan.studentregistration.activity.mapper.ActivityLogMapper;
import com.mahmoudramadan.studentregistration.activity.repo.ActivityLogRepository;
import com.mahmoudramadan.studentregistration.infra.security.CustomUserDetails;
import com.mahmoudramadan.studentregistration.shared.dto.PagedResponse;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ActivityLogMapper activityLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, Map<String, Object> details) {
        User actor = getCurrentUser();
        InetAddress ip = getClientIp();
        ActivityLog log = ActivityLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ip)
                .build();
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ActivityLogResponse> findAll(String entityType, String action, Long actorId, Pageable pageable) {
        Page<ActivityLogResponse> page;
        if (entityType != null) {
            page = activityLogRepository.findByEntityType(entityType, pageable)
                    .map(activityLogMapper::toResponse);
        } else if (action != null) {
            page = activityLogRepository.findByAction(action, pageable)
                    .map(activityLogMapper::toResponse);
        } else if (actorId != null) {
            page = activityLogRepository.findByActor_Id(actorId, pageable)
                    .map(activityLogMapper::toResponse);
        } else {
            page = activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(activityLogMapper::toResponse);
        }
        return PagedResponse.from(page);
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.getReferenceById(userDetails.getId());
        }
        return null;
    }
}
