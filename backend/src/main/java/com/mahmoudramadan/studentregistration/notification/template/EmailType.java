package com.mahmoudramadan.studentregistration.notification.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported email types, each mapped to an i18n subject key and a Thymeleaf
 * content fragment template.
 * <p>
 * The subject key is resolved via {@code MessageSource} at send time, allowing
 * locale-specific subject lines. The template value is combined with
 * {@code " :: content"} at render time to select the content fragment that
 * gets inserted into {@code emails/layout}.
 */
@Getter
@RequiredArgsConstructor
public enum EmailType {

    VERIFY_EMAIL(
            "Verifying email address",
            "emails/verify-email"),

    PASSWORD_RESET(
            "Resetting password",
            "emails/password-reset"),

    PASSWORD_CHANGED(
            "Password changed",
            "emails/password-changed"),

    WAITLIST_ENROLLED(
            "Waitlist to enrolled",
            "emails/waitlist-to-enrolled");

    private final String subjectKey;

    private final String template;

}