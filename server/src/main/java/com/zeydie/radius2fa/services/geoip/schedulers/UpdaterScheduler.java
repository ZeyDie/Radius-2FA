package com.zeydie.radius2fa.services.geoip.schedulers;

import com.zeydie.radius2fa.services.geoip.config.GeoIPConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.net.URL;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdaterScheduler {
    private final GeoIPConfig geoIPConfig;

    @SneakyThrows
    @Async
    @Scheduled(cron = "${app.geoip.database.update.cron}")
    public void updater() {
        if (!this.geoIPConfig.isUpdateEnabled())
            return;

        @NonNull val url = this.geoIPConfig.getUpdateUrl();
        @NonNull val path = this.geoIPConfig.getDatabasePath();

        @NonNull val downloadedPath = path.getParent()
                .resolve(url.substring(url.lastIndexOf('/') + 1));

        this.geoIPConfig.setDownloading(true);

        this.log.info("Updating GeoIP database...");

        FileUtils.copyURLToFile(
                new URL(url),
                downloadedPath.toFile()
        );

        try (
                @NonNull val in = Files.newInputStream(downloadedPath);
                @NonNull val inputBuffer = new BufferedInputStream(in);
                @NonNull val out = Files.newOutputStream(path);
                @NonNull val decompressor = new CompressorStreamFactory().createCompressorInputStream(inputBuffer)
        ) {
            this.log.info(
                    "Unzipping {} -> {}",
                    downloadedPath,
                    path
            );
            IOUtils.copy(decompressor, out);
        } finally {
            Files.deleteIfExists(downloadedPath);
        }

        this.log.info("GeoIP database updated");

        this.geoIPConfig.setDownloading(false);
    }
}