package com.mahmoudramadan.studentregistration.notification.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for email functionality, bound to the
 * {@code app.mail.*} prefix in YAML / environment variables.
 * <p>
 * Validated at startup — invalid or missing values produce clear error
 * messages rather than failing silently at runtime.
 */
@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    @Email
    @NotBlank
    private String from;

    @Email
    @NotBlank
    private String supportEmail;

    @NotBlank
    private String frontendUrl;

    @NotBlank
    private String applicationName;



}