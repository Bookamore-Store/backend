package com.bookamore.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Slf4j
@Configuration
@Conditional(SmtpConfiguredCondition.class)
@EnableConfigurationProperties(SmtpProperties.class)
@RequiredArgsConstructor
public class EmailConfig {

    private final SmtpProperties properties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.getHost());
        sender.setPort(properties.getPort());
        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());

        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", Boolean.toString(StringUtils.hasText(properties.getUsername())));
        mailProperties.put("mail.smtp.starttls.enable", "true");
        sender.setJavaMailProperties(mailProperties);

        log.info("EmailSenderService enabled: host={}, port={}", properties.getHost(), properties.getPort());
        return sender;
    }
}
