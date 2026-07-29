package com.mahmoudramadan.studentregistration.notification.sender;


import com.mahmoudramadan.studentregistration.notification.model.EmailMessage;

/**
 * Later can implement
 *
 * SMTP
 * SendGrid
 * Amazon SES
 * Mailgun
 * without changing MailService.
 */
public interface EmailSender {

    void send(EmailMessage message);

}