package com.mahmoudramadan.studentregistration.notification.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
public class EmailModel {

    private String username;

    private String applicationName;

    private String supportEmail;

    private String url;

    private String courseName;

}