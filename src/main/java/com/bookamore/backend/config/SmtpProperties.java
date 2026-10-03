package com.bookamore.backend.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString(exclude = "password")
@ConfigurationProperties(prefix = "mail.smtp")
public class SmtpProperties {
    private String host;
    private int port = 587;
    private String username;
    private String password;
}
