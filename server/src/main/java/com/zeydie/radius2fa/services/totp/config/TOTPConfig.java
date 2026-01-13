package com.zeydie.radius2fa.services.totp.config;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class TOTPConfig {
    @Getter
    @Value("${app.totp.issuer}")
    private String issuer;
    @Value("${app.totp.ntp}")
    private String ntp;
    @Getter
    @Value("${app.totp.digits}")
    private int digits;
    @Getter
    @Value("${app.totp.time.discrepancy}")
    private int timeDiscrepancy;
    @Getter
    @Value("${app.totp.time.period}")
    private int timePeriod;

    @Bean
    public @NotNull CodeVerifier codeVerifier() {
        @NonNull val codeVerifier = new DefaultCodeVerifier(this.codeGenerator(), this.timeProvider());

        if (this.timeDiscrepancy != 0)
            codeVerifier.setAllowedTimePeriodDiscrepancy(this.timeDiscrepancy);

        if (this.timePeriod != 0)
            codeVerifier.setTimePeriod(this.timePeriod);

        return codeVerifier;
    }

    @Bean
    public @NotNull CodeGenerator codeGenerator() {
        return new DefaultCodeGenerator(this.hashingAlgorithm(), this.digits);
    }

    @Bean
    public @NotNull HashingAlgorithm hashingAlgorithm() {
        return HashingAlgorithm.SHA512;
    }

    @SneakyThrows
    @Bean
    public @NotNull TimeProvider timeProvider() {
        return new NtpTimeProvider(this.ntp, Math.toIntExact(TimeUnit.SECONDS.toMillis(10)));
    }
}
