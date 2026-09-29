package com.bookamore.backend.service;

public interface EmailSenderService {

    void send(String to, String subject, String text);

    void sendHtml(String to, String subject, String html);
}
