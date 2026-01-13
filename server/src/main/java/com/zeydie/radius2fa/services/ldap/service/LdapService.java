package com.zeydie.radius2fa.services.ldap.service;

import com.zeydie.radius2fa.services.ldap.config.LdapConfig;
import com.zeydie.radius2fa.services.ldap.data.entities.UserLdapEntity;
import com.zeydie.radius2fa.services.ldap.repository.UserLdapRepsitory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

import static com.zeydie.radius2fa.services.ldap.config.LdapConfig.LOGIN_ATTRIBUTE;

@Log4j2
@RequiredArgsConstructor
@Service
public class LdapService {
    private final LdapConfig ldapConfig;

    private final UserLdapRepsitory userLdapRepsitory;
    private final LdapTemplate ldapTemplate;

    public @NotNull List<UserLdapEntity> getUsersWithVPNGroup() {
        return this.userLdapRepsitory.findByGroups(this.ldapConfig.getGroup());
    }

    public @Nullable UserLdapEntity getUser(@NonNull final String username) throws Throwable {
        return this.userLdapRepsitory.findByLogin(username)
                .orElseThrow((Supplier<Throwable>) () -> new RuntimeException("User not found"));
    }

    @SneakyThrows
    public boolean hasGroup(@NonNull final String username) {
        @Nullable val user = this.getUser(username);

        @NonNull val group = this.ldapConfig.getGroup();
        val hasGroup = user.getGroups()
                .stream()
                .anyMatch(s -> s.contains(group));

        if (!hasGroup) {
            log.warn("User {} has not group {}", username, group);
            return false;
        }

        return true;
    }

    @SneakyThrows
    public boolean authenticate(@NonNull final String username, @Nullable final String password) {
        @NonNull val filter = new AndFilter();

        filter.and(new EqualsFilter(LOGIN_ATTRIBUTE, username));

        return this.ldapTemplate.authenticate("", filter.toString(), password);
    }
}