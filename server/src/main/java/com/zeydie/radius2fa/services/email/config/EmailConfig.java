package com.zeydie.radius2fa.services.email.config;

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
    @Getter
    @Value("${app.mail.template.notify-login}")
    private String notifyLoginTemplate;
    @Getter
    @Value("${app.mail.template.notify-logged}")
    private String notifyLoggedTemplate;
}