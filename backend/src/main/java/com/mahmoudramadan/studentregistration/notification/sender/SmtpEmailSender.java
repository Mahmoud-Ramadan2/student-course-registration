package com.mahmoudramadan.studentregistration.notification.sender;

import com.mahmoudramadan.studentregistration.notification.exception.MailSendingException;
import com.mahmoudramadan.studentregistration.notification.model.EmailMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "app.mail",
        name = "provider",
        havingValue = "smtp",
        matchIfMissing = true
)
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    @CircuitBreaker(
            name = "emailBreaker",
            fallbackMethod = "sendEmailFallback"
    )
    @Retry(name = "emailRetry")
    public void send(EmailMessage email) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(email.getFrom());
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            if (email.getPlainTextBody() != null) {
                helper.setText(email.getPlainTextBody(), email.getHtmlBody());
            } else {
                helper.setText(email.getHtmlBody(), true);
            }
            mailSender.send(mime);
            log.debug("Email sent successfully to {}", email.getTo());
        } catch (MailSendException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MailSendingException("Unable to send email to " + email.getTo(), ex);
        }
    }

    /**
     * Fallback invoked when retries are exhausted or the circuit breaker is open.
     * Logs the failure and records a metric. Does <b>not</b> rethrow, so the
     * async listener completes without error despite the delivery failure.
     */
    public void sendEmailFallback(EmailMessage email, Throwable exception) {

        log.error("Failed to send email to {} after retries and circuit breaker: {}",
                email.getTo(), exception.getMessage());
    }

}
