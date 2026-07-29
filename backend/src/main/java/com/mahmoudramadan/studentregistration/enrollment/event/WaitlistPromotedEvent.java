package com.mahmoudramadan.studentregistration.enrollment.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WaitlistPromotedEvent extends ApplicationEvent {
    private final String studentEmail;
    private final String studentUsername;
    private final String courseTitle;
    private final String termName;

    public WaitlistPromotedEvent(Object source, String studentEmail,
                                 String studentUsername, String courseTitle, String termName) {

        super(source);
        this.studentEmail = studentEmail;
        this.studentUsername = studentUsername;
        this.courseTitle = courseTitle;
        this.termName = termName;
    }
}
