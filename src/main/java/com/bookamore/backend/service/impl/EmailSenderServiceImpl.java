package com.bookamore.backend.service.impl;

import com.bookamore.backend.config.SmtpConfiguredCondition;
import com.bookamore.backend.config.SmtpProperties;
import com.bookamore.backend.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Service
@Conditional(SmtpConfiguredCondition.class)
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final SmtpProperties smtpProperties;

    @Override
    public void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(smtpProperties.getUsername())) {
            message.setFrom(smtpProperties.getUsername());
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            if (StringUtils.hasText(smtpProperties.getUsername())) {
                helper.setFrom(smtpProperties.getUsername());
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new MailSendException("Failed to send email to " + to, ex);
        }
    }
}
