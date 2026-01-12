package com.zeydie.radius2fa.totp.config;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TOTPConfig {
    @Getter
    @Value("${app.totp.issuer}")
    private String issuer;

    @Bean
    public CodeVerifier codeVerifier() {
        return new DefaultCodeVerifier(this.codeGenerator(), this.timeProvider());
    }

    @Bean
    public CodeGenerator codeGenerator() {
        return new DefaultCodeGenerator(this.hashingAlgorithm());
    }

    @Bean
    public HashingAlgorithm hashingAlgorithm() {
        return HashingAlgorithm.SHA256;
    }

    @SneakyThrows
    @Bean
    public TimeProvider timeProvider() {
        return new NtpTimeProvider("pool.ntp.org");
    }
}
