package com.mahmoudramadan.studentregistration.notification.template;

import com.mahmoudramadan.studentregistration.notification.exception.TemplateRenderingException;
import com.mahmoudramadan.studentregistration.notification.model.EmailModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;

/**
 * Renders Thymeleaf email templates into HTML strings using a shared layout.
 * <p>
 * First renders the type-specific content template independently
 * (e.g. {@code emails/verify-email}), then inserts the resulting HTML into
 * the {@code emails/layout} template via the {@code contentHtml} context
 * variable. This ensures consistent header, footer, and CSS across all email
 * types without duplicating markup.
 */
@Component
@RequiredArgsConstructor
public class TemplateRenderer {
    private final SpringTemplateEngine templateEngine;


    public String render(EmailType emailType, EmailModel model, String subject) {
        try {
            Context context = new Context();
            context.setVariable("model", model);
            context.setVariable("subject", subject);

            String contentHtml = templateEngine.process(emailType.getTemplate(), context);
            context.setVariable("contentHtml", contentHtml);

            return templateEngine.process("emails/layout", context);
        } catch (Exception ex) {
            throw new TemplateRenderingException("Unable to render template " + emailType, ex);
        }
    }
}