package com.ebv14.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRecoveryCode(String email, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("FinanzAPP - Código de recuperación");
        message.setText("Tu código de recuperación es: " + codigo +
                "\n\nEste código expira en 15 minutos." +
                "\n\nSi no solicitaste este código, ignora este mensaje.");
        mailSender.send(message);
    }
}
