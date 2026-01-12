package com.zeydie.radius2fa.email.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    @Getter
    @Value("${spring.mail.username}")
    private String username;

    @Getter
    @Value("${app.mail.template.qr-code}")
    private String qrCodeTemplate;
}