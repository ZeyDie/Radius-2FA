package com.zeydie.radius2fa.totp.service;

import com.zeydie.radius2fa.email.services.QrCodeEmailService;
import com.zeydie.radius2fa.ldap.service.LdapService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class SecretService {
    private final SecretGenerator secretGenerator;
    private final QrCodeEmailService qrCodeEmailService;

    private final LdapService ldapService;
    private final UserSecretService userSecretService;
    private final QrCodeService qrCodeService;
    private final CodeVerifier codeVerifier;

    public @NotNull String encode(@NonNull final String secret) {
        return new String(Base64.encodeBase64(secret.getBytes(StandardCharsets.UTF_8)));
    }

    public @NotNull String dencode(@NonNull final String secret) {
        return new String(Base64.decodeBase64(secret.getBytes(StandardCharsets.UTF_8)));
    }

    public @NotNull String generateTOTP() {
        return this.secretGenerator.generate();
    }

    public @NotNull String generateTOTPWithQrCode(@NonNull final Name id, @NonNull final String login, @NonNull final String email) {
        this.log.info("Generating secret for {}", login);

        @NonNull val secret = this.generateTOTP();

        try {
            val data = this.qrCodeService.generateQrCode(login, secret);

            this.setSecretTOTP(id, secret);

            //this.qrCodeEmailService.sendQrCodeToEmail(email, data);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

        return secret;
    }

    public boolean hasSecretTOTP(@NonNull final Name id) {
        return this.userSecretService.hasSecretTOTP(id);
    }

    public boolean hasSecretTOTP(@NonNull final String username) {
        return this.userSecretService.hasSecretTOTP(username);
    }

    public boolean verifyTOTPOfLogin(@NonNull final String login, @NonNull final String code) {
        @Nullable val secret = this.getSecretOfLogin(login);

        return this.verifyTOTPOfSecret(secret, code);
    }

    public boolean verifyTOTPOfSecret(@NonNull final String secret, @NonNull final String code) {
        return this.codeVerifier.isValidCode(this.dencode(secret), code);
    }

    public @Nullable String getSecretOfLogin(@NonNull final String login) {
        return this.userSecretService.getSecretTOTP(login);
    }

    public void setSecretTOTP(@NonNull final Name id, @NonNull final String secret) throws Exception {
        this.userSecretService.setSecretTOTP(id, this.encode(secret));
    }

    public void removeSecretTOTP(@NonNull final Name id) throws Exception {
        this.userSecretService.removeSecretTOTP(id);
    }
}