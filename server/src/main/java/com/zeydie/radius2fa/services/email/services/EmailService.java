package com.zeydie.radius2fa.services.email.services;

import com.zeydie.radius2fa.services.email.config.EmailConfig;
import com.zeydie.radius2fa.services.geoip.service.GeoIPService;
import com.zeydie.radius2fa.services.ldap.service.LdapService;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final EmailConfig emailConfig;

    private final DefaultEmailService defaultEmailService;

    private final LdapService ldapService;
    private final GeoIPService geoIPService;

    private String qrCodeTemplate;
    private String notifyLoginTemplate;
    private String notifyLoggedTemplate;

    @PostConstruct
    public void read() {
        try {
            this.qrCodeTemplate = String.join("\n", Files.readAllLines(Paths.get(this.emailConfig.getQrCodeTemplate())));
            this.notifyLoginTemplate = String.join("\n", Files.readAllLines(Paths.get(this.emailConfig.getNotifyLoginTemplate())));
            this.notifyLoggedTemplate = String.join("\n", Files.readAllLines(Paths.get(this.emailConfig.getNotifyLoggedTemplate())));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private @Nullable String getEmailOfUser(@NonNull final String login) {
        @Nullable val user = this.ldapService.getUser(login);

        if (user != null)
            return user.getEmail();

        return null;
    }

    public void sendQrCodeToEmail(@NonNull final String email, final byte[] data) {
        @Nullable var template = this.qrCodeTemplate;

        if (template == null)
            throw new RuntimeException("Template not found");

        template = template.replaceAll("%qr-code-data%", Base64.getEncoder().encodeToString(data));

        this.log.info("Send QR code to email {}", email);

        this.defaultEmailService.sendEmail(
                email,
                "Установка 2-х факторной аутентификации",
                template
        );
    }

    public void sendNotifyLoginToEmail(@NonNull final String login, @NonNull final InetAddress inetAddress, final boolean passwordIsCorrectly) {
        @Nullable val email = this.getEmailOfUser(login);

        if (email == null) {
            this.log.error("User {} not found", login);
            return;
        }

        @Nullable var template = this.notifyLoginTemplate;

        if (template == null)
            throw new RuntimeException("Template not found");

        template = template.replaceAll("%password-correctly%", passwordIsCorrectly ? "Пароль введен верный" : "Пароль введен неверный");

        try {
            @Nullable val cityResponse = this.geoIPService.read(inetAddress);

            if (cityResponse != null) {
                @NonNull val country = cityResponse.getCountry();
                @NonNull val location = cityResponse.getLocation();

                @NonNull val isoCode = country.getIsoCode();

                template = template.replaceAll("%location-flag%", isoCode);
                template = template.replaceAll("%location-country%", country.getName());
                template = template.replaceAll("%location-city%", cityResponse.getCity().getName());
                template = template.replaceAll("%location-latitude%", String.valueOf(location.getLatitude()));
                template = template.replaceAll("%location-longitude%", String.valueOf(location.getLongitude()));
            } else
                template = template.replaceAll("%location-flag%, %location-country%, %location-city%, %location-latitude% %location-longitude%", "Не удалось определить геолокацию");
        } catch (final Exception e) {
            e.printStackTrace();
        }

        template = template.replaceAll("%ip%", inetAddress.getHostAddress());

        template = template.replaceAll("%date%", new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(Date.from(Instant.now())));

        this.log.info("Send notify login to email {}", email);

        this.defaultEmailService.sendEmail(
                email,
                "Уведомление о попытке входа",
                template
        );
    }

    public void sendNotifyLoggedToEmail(@NonNull final String login) {
        @Nullable val email = this.getEmailOfUser(login);

        if (email == null) {
            this.log.error("User {} not found", login);
            return;
        }

        @Nullable val template = this.notifyLoggedTemplate;

        if (template == null)
            throw new RuntimeException("Template not found");

        this.log.info("Send notify logged to email {}", email);

        this.defaultEmailService.sendEmail(
                email,
                "Уведомление о входе",
                template
        );
    }
}