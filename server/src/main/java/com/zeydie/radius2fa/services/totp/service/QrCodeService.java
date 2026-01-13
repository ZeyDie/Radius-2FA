package com.zeydie.radius2fa.services.totp.service;

import com.zeydie.radius2fa.services.totp.config.TOTPConfig;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                        .digits(this.totpConfig.getDigits())
                        .issuer(this.totpConfig.getIssuer())
                        .algorithm(this.totpConfig.hashingAlgorithm())
                        .build()
        );
    }
}