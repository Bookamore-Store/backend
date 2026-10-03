package com.bookamore.backend.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Matches when an SMTP host is configured. An empty {@code SMTP_HOST} keeps
 * {@link com.bookamore.backend.service.EmailSenderService} out of the context.
 */
public class SmtpConfiguredCondition implements Condition {

    static final String HOST_PROPERTY = "mail.smtp.host";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return StringUtils.hasText(context.getEnvironment().getProperty(HOST_PROPERTY));
    }
}
