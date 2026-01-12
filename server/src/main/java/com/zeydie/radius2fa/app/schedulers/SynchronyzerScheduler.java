package com.zeydie.radius2fa.app.schedulers;

import com.google.common.collect.Lists;
import com.zeydie.radius2fa.ldap.config.LdapConfig;
import com.zeydie.radius2fa.ldap.data.entities.mapper.UserLdapMapper;
import com.zeydie.radius2fa.ldap.service.LdapService;
import com.zeydie.radius2fa.totp.entity.UserSecretEntity;
import com.zeydie.radius2fa.totp.service.SecretService;
import com.zeydie.radius2fa.totp.service.UserSecretService;
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
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void sync() {
        @NonNull val usersWithVPNGroup = this.ldapService.getUsersWithVPNGroup();

        if (!this.cachedUsersWithVPNGroup.equals(usersWithVPNGroup)) {
            @NonNull val deletedUsers = this.cachedUsersWithVPNGroup.stream()
                    .filter(
                            user -> usersWithVPNGroup.stream()
                                    .noneMatch(
                                            userLdapEntity -> userLdapEntity.getId().equals(user.getId()) &&
                                                    userLdapEntity.getLogin().equals(user.getLogin()) &&
                                                    userLdapEntity.getEmail().equals(user.getEmail())
                                    )
                    )
                    .toList();

            if (!deletedUsers.isEmpty()) {
                deletedUsers.forEach(
                        user -> {
                            try {
                                this.secretService.removeSecretTOTP(user.getId());
                                this.cachedUsersWithVPNGroup.remove(user);
                                this.log.info("Deleted user: {}", user);
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                );
            }

            @NonNull val newUsers = usersWithVPNGroup.stream()
                    .map(userLdapEntity -> UserLdapMapper.INSTANCE.toUserSecretEntity(userLdapEntity))
                    .filter(user -> !this.cachedUsersWithVPNGroup.contains(user))
                    .toList();

            if (!newUsers.isEmpty())
                newUsers.forEach(
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
    }

    private void updateCachedUsers() {
        this.cachedUsersWithVPNGroup = this.userSecretService.findAll();
    }
}