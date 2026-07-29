package com.mahmoudramadan.studentregistration.notification.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessage {

    private String from;

    private String to;

    private String subject;

    private String htmlBody;

    private String plainTextBody;

}