package com.mahmoudramadan.studentregistration.notification.exception;

public class MailSendingException extends NotificationException {

    public MailSendingException(
            String message,
            Throwable cause) {

        super(message, cause);
    }

}