package com.zeydie.radius2fa.ldap.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapConfig {
    @Getter
    @Value("${app.radius.group}")
    private String group;

    public static final @NotNull String LOGIN_ATTRIBUTE = "sAMAccountName";
    public static final @NotNull String EMAIL_ATTRIBUTE = "mail";
    public static final @NotNull String GROUPS_ATTRIBUTE = "memberOf";
    public static final @NotNull String SECRET_TOTP_ATTRIBUTE = "info";
}