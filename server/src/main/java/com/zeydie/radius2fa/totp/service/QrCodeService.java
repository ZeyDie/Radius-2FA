package com.zeydie.radius2fa.totp.service;

import com.zeydie.radius2fa.totp.config.TOTPConfig;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class QrCodeService {
    private final TOTPConfig totpConfig;

    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;

    public byte[] generateQrCode(@NonNull final String login, @NonNull final String secret) throws QrGenerationException {
        return this.qrGenerator.generate(
                this.qrDataFactory.newBuilder()
                        .label(login)
                        .secret(secret)
                        .issuer(this.totpConfig.getIssuer())
                        .build()
        );
    }
}