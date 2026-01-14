package com.zeydie.radius2fa.services.geoip.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.zeydie.radius2fa.services.geoip.config.GeoIPConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
@Service
public class GeoIPService {
    private final GeoIPConfig geoIPConfig;

    private DatabaseReader databaseReader;

    private void initializeDatabaseReader() throws IOException {
        this.databaseReader = new DatabaseReader.Builder(Files.newInputStream(this.geoIPConfig.getDatabasePath())).build();
    }

    public @Nullable CityResponse read(@NonNull final String ip) throws UnknownHostException {
        return this.read(InetAddress.getByName(ip));
    }

    public @Nullable CityResponse read(@NonNull final InetAddress inetAddress) {
        if (this.geoIPConfig.isDownloading()) {
            this.log.warn("GeoIP database is downloading");
            return null;
        }

        try {
            if (this.databaseReader == null)
                this.initializeDatabaseReader();

            return this.databaseReader.city(inetAddress);
        } catch (final IOException | GeoIp2Exception exception) {
            this.log.error(exception.getMessage(), exception);
        }

        return null;
    }
}