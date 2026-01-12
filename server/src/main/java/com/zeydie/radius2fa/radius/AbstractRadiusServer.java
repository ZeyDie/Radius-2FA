package com.zeydie.radius2fa.radius;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusServer;

import java.net.InetSocketAddress;

@Log4j2
@RequiredArgsConstructor
public abstract class AbstractRadiusServer extends RadiusServer {
    @Value("${app.radius.secret}")
    private String radiusSecret;

    public void startRadiusServer() {
        this.log.info("Starting RADIUS server");
        this.log.info("Auth Port: {}", +this.getAuthPort());
        this.log.info("Acct Port: {}", +this.getAcctPort());

        this.start(true, false);
    }

    @Override
    public @Nullable String getSharedSecret(@NonNull final InetSocketAddress inetSocketAddress) {
        return this.radiusSecret;
    }

    @Override
    public @Nullable String getUserPassword(@NonNull final String username) {
        return null;
    }

    protected abstract @NotNull RadiusPacket handlePasswordRequest(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String login,
            @Nullable final String password
    );

    protected abstract @NotNull RadiusPacket handleOTPRequest(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String login,
            @NonNull final String password
    );

    protected abstract @NotNull RadiusPacket createAcceptResponse(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String login
    );

    protected abstract @NotNull RadiusPacket createRejectResponse(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String message
    );
}