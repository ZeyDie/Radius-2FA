package com.zeydie.radius2fa.services.ldap.repository;

import com.zeydie.radius2fa.services.ldap.data.entities.UserLdapEntity;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLdapRepsitory extends LdapRepository<UserLdapEntity> {
    @NotNull Optional<UserLdapEntity> findByLogin(@NonNull final String username);

    @NotNull List<UserLdapEntity> findByGroups(@NonNull final String groupName);
}