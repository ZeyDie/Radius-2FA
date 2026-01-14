package com.zeydie.radius2fa.app.schedulers;

import com.google.common.collect.Lists;
import com.zeydie.radius2fa.services.ldap.config.LdapConfig;
import com.zeydie.radius2fa.services.ldap.data.entities.mapper.UserLdapMapper;
import com.zeydie.radius2fa.services.ldap.service.LdapService;
import com.zeydie.radius2fa.services.totp.entity.UserSecretEntity;
import com.zeydie.radius2fa.services.totp.service.SecretService;
import com.zeydie.radius2fa.services.totp.service.UserSecretService;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class SynchronyzerScheduler {
    private final LdapConfig ldapConfig;

    private final LdapService ldapService;
    private final SecretService secretService;
    private final UserSecretService userSecretService;

    private @NotNull List<UserSecretEntity> cachedUsersWithVPNGroup = Lists.newArrayList();

    @PostConstruct
    public void init() {
        this.updateCachedUsers();
    }

    @Async
    @Scheduled(cron = "${app.crons.sync}")
    public void sync() {
        try {
            @NonNull val usersWithVPNGroup = this.ldapService.getUsersWithVPNGroup();

            if (!this.cachedUsersWithVPNGroup.equals(usersWithVPNGroup)) {
                this.log.debug("Users with VPN group changed");

                this.cachedUsersWithVPNGroup.stream()
                        .filter(
                                user -> usersWithVPNGroup.stream()
                                        .noneMatch(
                                                userLdapEntity -> userLdapEntity.getId().equals(user.getId()) &&
                                                        userLdapEntity.getLogin().equals(user.getLogin()) &&
                                                        userLdapEntity.getEmail().equals(user.getEmail())
                                        )
                        )
                        .forEach(
                                user -> {
                                    try {
                                        this.secretService.removeSecretTOTP(user.getId());
                                        this.cachedUsersWithVPNGroup.remove(user);
                                        this.log.info("Deleted user: {}", user);
                                    } catch (final Exception exception) {
                                        this.log.error(exception.getMessage(), exception);
                                    }
                                }
                        );

                usersWithVPNGroup.stream()
                        .map(userLdapEntity -> UserLdapMapper.INSTANCE.toUserSecretEntity(userLdapEntity))
                        .filter(user -> !this.cachedUsersWithVPNGroup.contains(user))
                        .forEach(
                                userSecretEntity -> {
                                    this.userSecretService.save(userSecretEntity);
                                    this.cachedUsersWithVPNGroup.add(userSecretEntity);
                                    this.log.info("New user: {}", userSecretEntity);
                                }
                        );
            }

            @NonNull val usersWithoutSecretTOTP = this.cachedUsersWithVPNGroup.stream()
                    .filter(user -> !this.secretService.hasSecretTOTP(user.getId()))
                    .toList();

            if (!usersWithoutSecretTOTP.isEmpty()) {
                usersWithoutSecretTOTP.forEach(user -> this.secretService.generateTOTPWithQrCode(user.getId(), user.getLogin(), user.getEmail()));

                this.updateCachedUsers();
            }
        } catch (final Exception exception) {
            this.log.error(exception.getMessage(), exception);
        }
    }

    private void updateCachedUsers() {
        this.cachedUsersWithVPNGroup = this.userSecretService.findAll();
    }
}