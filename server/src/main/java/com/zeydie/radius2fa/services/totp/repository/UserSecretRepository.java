package com.zeydie.radius2fa.services.totp.repository;

import com.zeydie.radius2fa.services.totp.entity.UserSecretEntity;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.naming.Name;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSecretRepository extends JpaRepository<UserSecretEntity, Name> {
    @NotNull Optional<UserSecretEntity> findByLogin(@NonNull final String login);

    @NotNull List<UserSecretEntity> findUserSecretEntitiesBySecretTOTPNotNull();
}