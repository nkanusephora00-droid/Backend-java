package com.itaccess.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@itaccess.local}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Réinitialisation de votre mot de passe - IT Access");
            
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            String emailBody = String.format(
                "Bonjour %s,\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe:\n" +
                "%s\n\n" +
                "Ce lien expire dans 30 minutes.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe IT Access",
                username, resetLink
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }
    
    public void sendEmailVerification(String toEmail, String username, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Vérification de votre email - IT Access");
            
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
            
            String emailBody = String.format(
                "Bonjour %s,\n\n" +
                "Merci de vous être inscrit sur IT Access.\n\n" +
                "Cliquez sur le lien ci-dessous pour vérifier votre email:\n" +
                "%s\n\n" +
                "Ce lien expire dans 24 heures.\n\n" +
                "Cordialement,\n" +
                "L'équipe IT Access",
                username, verificationLink
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Email de vérification envoyé à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de vérification", e);
        }
    }
}
