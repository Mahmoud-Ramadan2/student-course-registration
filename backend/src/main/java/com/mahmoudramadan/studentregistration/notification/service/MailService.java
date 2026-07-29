package com.mahmoudramadan.studentregistration.notification.service;

import com.mahmoudramadan.studentregistration.notification.config.MailProperties;
import com.mahmoudramadan.studentregistration.notification.model.EmailMessage;
import com.mahmoudramadan.studentregistration.notification.model.EmailModel;
import com.mahmoudramadan.studentregistration.notification.sender.EmailSender;
import com.mahmoudramadan.studentregistration.notification.template.EmailType;
import com.mahmoudramadan.studentregistration.notification.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Orchestrates the email-sending pipeline for a given recipient, type, and model.
 * <p>
 * Resolves the i18n subject line via {@link MessageSource}, renders the HTML
 * body via {@link TemplateRenderer}, builds an {@link EmailMessage}, and
 * delegates delivery to the configured {@link EmailSender} implementation.
 * The locale is obtained from {@link LocaleContextHolder} which is propagated
 * to async threads by {@code LocaleContextTaskDecorator}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final EmailSender sender;
    private final TemplateRenderer renderer;
    private final MailProperties properties;

    public void send(String to, EmailType type, EmailModel model) {
        log.debug("Sending email to={} type={}", to, type);
        String subject = type.getSubjectKey();
        String html = renderer.render(type, model, subject);

        EmailMessage message = EmailMessage.builder()
                .from(properties.getFrom())
                .to(to)
                .subject(subject)
                .htmlBody(html)
                .plainTextBody(null)
                .build();
        sender.send(message);
    }
}