package com.zeydie.radius2fa.email.services;

import com.zeydie.radius2fa.email.config.EmailConfig;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class QrCodeEmailService {
    private final EmailConfig emailConfig;

    private final DefaultEmailService defaultEmailService;

    private String qrCodeTemplate;

    @PostConstruct
    public void read() {
        try {
            this.qrCodeTemplate = String.join("\n", Files.readAllLines(Paths.get(this.emailConfig.getQrCodeTemplate())));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void sendQrCodeToEmail(@NonNull final String email, final byte[] data) {
        @NonNull var template = this.qrCodeTemplate;

        template = template.replaceAll("%qr-code-data%", Base64.getEncoder().encodeToString(data));

        this.log.info("Send QR code to email {}", email);

        this.defaultEmailService.sendEmail(
                email,
                "Установка 2-х факторной аутентификации",
                template
        );
    }
}