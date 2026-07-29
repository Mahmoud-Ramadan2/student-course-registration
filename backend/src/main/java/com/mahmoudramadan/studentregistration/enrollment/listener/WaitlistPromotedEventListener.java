package com.mahmoudramadan.studentregistration.enrollment.listener;

import com.mahmoudramadan.studentregistration.enrollment.event.WaitlistPromotedEvent;
import com.mahmoudramadan.studentregistration.notification.config.MailProperties;
import com.mahmoudramadan.studentregistration.notification.model.EmailModel;
import com.mahmoudramadan.studentregistration.notification.service.MailService;
import com.mahmoudramadan.studentregistration.notification.template.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// enrollment/listener/WaitlistPromotedEventListener.java
@Component
@RequiredArgsConstructor
@Slf4j
public class WaitlistPromotedEventListener implements ApplicationListener<WaitlistPromotedEvent> {

    private final MailService mailService;
    private final MailProperties mailProperties;

    @Override
    @Async
    public void onApplicationEvent(WaitlistPromotedEvent event) {
        try {
            EmailModel model = EmailModel.builder()
                    .username(event.getStudentUsername())
                    .courseName(event.getCourseTitle())
                    .applicationName(mailProperties.getApplicationName())
                    .supportEmail(mailProperties.getSupportEmail())
                    .build();

            mailService.send(event.getStudentEmail(), EmailType.WAITLIST_ENROLLED, model);
        } catch (Exception ex) {
            log.error("Failed to send waitlist promotion email to {}", event.getStudentEmail(), ex);
        }
    }
}
