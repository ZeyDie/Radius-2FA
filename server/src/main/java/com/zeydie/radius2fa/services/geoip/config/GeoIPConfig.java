package com.zeydie.radius2fa.services.geoip.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Configuration
public class GeoIPConfig {
    @Value("${app.geoip.database.update.enabled}")
    private boolean updateEnabled;

    @Value("${app.geoip.database.url}")
    private String updateUrl;

    @Value("${app.geoip.database.path}")
    private String databasePath;

    @Setter
    private boolean downloading;

    public @NotNull Path getDatabasePath() {
        return Paths.get(".", this.databasePath);
    }
}
