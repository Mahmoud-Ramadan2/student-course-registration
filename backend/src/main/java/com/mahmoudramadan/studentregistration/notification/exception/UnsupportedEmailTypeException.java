package com.mahmoudramadan.studentregistration.notification.exception;


import com.mahmoudramadan.studentregistration.notification.template.EmailType;

public class UnsupportedEmailTypeException extends NotificationException {

    public UnsupportedEmailTypeException(EmailType type) {

        super("Unsupported email type: " + type);

    }

}