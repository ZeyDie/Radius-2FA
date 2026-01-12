package com.zeydie.radius2fa.radius.servers;

import com.zeydie.radius2fa.ldap.service.LdapService;
import com.zeydie.radius2fa.radius.AbstractRadiusServer;
import com.zeydie.radius2fa.totp.service.QrCodeService;
import com.zeydie.radius2fa.totp.service.SecretService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Component
public class FortiGateRadiusServer extends AbstractRadiusServer {
    private final LdapService ldapService;
    private final SecretService secretService;
    private final QrCodeService qrCodeService;

    @SneakyThrows
    @Override
    public @NotNull RadiusPacket accessRequestReceived(
            @NonNull final AccessRequest accessRequest,
            @NonNull final InetSocketAddress client
    ) {
        @NonNull val login = accessRequest.getUserName();
        @Nullable val password = accessRequest.getUserPassword();
        @Nullable val state = accessRequest.getAttributeValue("State");

        this.log.info(
                "[{}] received access request from client={}, login={}, password={}",
                state,
                client,
                login,
                Optional.ofNullable(password)
                        .map(s -> "*".repeat(s.length()))
                        .orElse(null)
        );

        if (state == null)
            return this.handlePasswordRequest(accessRequest, login, password);

        return this.handleOTPRequest(accessRequest, login, password);
    }

    @Override
    protected @NotNull RadiusPacket handlePasswordRequest(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String login,
            @Nullable final String password
    ) {
        if (!this.ldapService.hasGroup(login)) {
            this.log.warn("User {} has no group", login);

            return this.createRejectResponse(accessRequest, "User has no access");
        }

        if (!this.secretService.hasSecretTOTP(login))
            return this.createRejectResponse(accessRequest, "Secret TOTP not found for user " + login);

        try {
            val passwordValid = this.ldapService.authenticate(login, password);

            this.log.info(
                    "Password is valid for {} {}",
                    login,
                    passwordValid ? "YES" : "NO"
            );

            if (passwordValid) {
                @NonNull val packet = new RadiusPacket(RadiusPacket.ACCESS_CHALLENGE, accessRequest.getPacketIdentifier());

                @NonNull val stateAttribute = RadiusAttribute.createRadiusAttribute(24);
                stateAttribute.setAttributeData("TOTP".getBytes(StandardCharsets.UTF_8));

                packet.addAttribute(stateAttribute);
                packet.addAttribute("Reply-Message", "Enter 2FA code");
                packet.addAttribute("Session-Timeout", "300");

                this.log.info("Sending challenge packet to {}", login);

                return packet;
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

        return this.createRejectResponse(accessRequest, "Invalid username or password");
    }

    @Override
    protected @NotNull RadiusPacket handleOTPRequest(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String login,
            @NonNull final String password
    ) {
        this.log.debug("handleOTPRequest {}", accessRequest);

        val otpValid = this.secretService.verifyTOTPOfLogin(login, password);

        if (otpValid) {
            this.log.info("OTP is valid for {}", login);

            return this.createAcceptResponse(accessRequest, login);
        } else {
            this.log.info("OTP is invalid for {}", login);

            return this.createRejectResponse(accessRequest, "Invalid TOTP code");
        }
    }

    @Override
    protected @NotNull RadiusPacket createAcceptResponse(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String login
    ) {
        @NonNull val packet = new RadiusPacket(RadiusPacket.ACCESS_ACCEPT, accessRequest.getPacketIdentifier());

        packet.addAttribute("Reply-Message", "Authentication successful");

        this.copyProxyState(accessRequest, packet);

        return packet;
    }

    @Override
    protected @NotNull RadiusPacket createRejectResponse(
            @NonNull final AccessRequest accessRequest,
            @NonNull final String message
    ) {
        @NonNull val packet = new RadiusPacket(RadiusPacket.ACCESS_REJECT, accessRequest.getPacketIdentifier());

        packet.addAttribute("Reply-Message", message);

        this.copyProxyState(accessRequest, packet);

        return packet;
    }

    private boolean isLocalNAS(@Nullable final String nasIdentifier, @NonNull final String login, @NonNull final InetSocketAddress client) {
        /*log.debug("Checking local NAS {} {} {}", nasIdentifier, login, client.getAddress().getHostAddress());

        if (nasIdentifier == null) return false;

        val isIdentifierNAS = this.nasIdentifiers.contains(nasIdentifier);
        val isLoginNAS = this.nasLogin.equals(login);
        val isIpNAS = this.nasIps.contains(client.getAddress().getHostAddress());

        log.debug("Is local NAS {} {} {}", isIdentifierNAS, isIpNAS, isLoginNAS);

        return isIdentifierNAS && isIpNAS && isLoginNAS;*/
        return false;
    }
}