package com.zeydie.radius2fa.app;

import com.zeydie.radius2fa.radius.AbstractRadiusServer;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@AllArgsConstructor
@ComponentScans(value = {@ComponentScan("com.zeydie"), @ComponentScan("dev.samstevens")})
@EntityScan("com.zeydie")
@EnableLdapRepositories("com.zeydie")
@EnableJpaRepositories("com.zeydie")
@EnableScheduling
@EnableAutoConfiguration
@SpringBootApplication
public class Application {
    private final AbstractRadiusServer radiusServer;

    public static void main(@Nullable final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startRadiusServer() {
        this.radiusServer.startRadiusServer();
    }
}