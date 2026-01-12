package com.zeydie.radius2fa.email.services;

import com.zeydie.radius2fa.email.config.EmailConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@RequiredArgsConstructor
@Service
public class DefaultEmailService {
    private final EmailConfig emailConfig;

    private final JavaMailSender emailSender;

    public void sendEmail(
            @NonNull final String address,
            @NonNull final String subject,
            @NonNull final String message
    ) {
        this.sendEmail(new String[]{address}, null, subject, message);
    }

    public void sendEmail(
            @NonNull final String[] address,
            @Nullable final String[] copy,
            @NonNull final String subject,
            @NonNull final String message
    ) {
        this.sendEmail(address, copy, subject, message, null);
        /*
         @NonNull val mailMessage = new SimpleMailMessage();

        mailMessage.setTo(address);
        mailMessage.setCc(copy);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        this.emailSender.send(mailMessage);
         */
    }

    @SneakyThrows
    public void sendEmail(
            @NonNull final String[] address,
            @Nullable final String[] copy,
            @NonNull final String subject,
            @NonNull final String html,
            @Nullable final String[] attachments
    ) {
        @NonNull val mimeMessage = this.emailSender.createMimeMessage();
        @NonNull val mailMessage = new MimeMessageHelper(mimeMessage, true);

        mailMessage.setFrom(this.emailConfig.getUsername());
        mailMessage.setTo(address);

        if (copy != null)
            mailMessage.setCc(copy);

        mailMessage.setSubject(subject);
        mailMessage.setText(html, true);

        if (attachments != null)
            for (val attachment : attachments) {
                @NonNull val file = Paths.get(attachment).toFile();

                if (!file.exists())
                    throw new IllegalArgumentException("File not found: " + attachment);

                mailMessage.addAttachment(file.getName(), file);
            }

        this.emailSender.send(mimeMessage);
    }
}