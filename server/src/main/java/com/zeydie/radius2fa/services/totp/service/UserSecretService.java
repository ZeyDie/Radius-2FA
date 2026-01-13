package com.zeydie.radius2fa.services.totp.service;

import com.zeydie.radius2fa.services.totp.entity.UserSecretEntity;
import com.zeydie.radius2fa.services.totp.repository.UserSecretRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.List;
import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserSecretService {
    private final UserSecretRepository userSecretRepository;

    public @NotNull List<UserSecretEntity> findAll() {
        return this.userSecretRepository.findAll();
    }

    @SneakyThrows
    public @NotNull UserSecretEntity findById(@NonNull final Name id) {
        return this.userSecretRepository.findById(id)
                .orElseThrow((Supplier<Throwable>) () -> new NullPointerException(String.format("User %s not found", id)));
    }

    @SneakyThrows
    public @NotNull UserSecretEntity findById(@NonNull final String login) {
        return this.userSecretRepository.findByLogin(login)
                .orElseThrow((Supplier<Throwable>) () -> new NullPointerException(String.format("User %s not found", login)));
    }

    public @NotNull List<UserSecretEntity> getUsersWithSecretTOTP() {
        return this.userSecretRepository.findUserSecretEntitiesBySecretTOTPNotNull();
    }

    public void save(@NonNull final UserSecretEntity userSecretEntity) {
        this.userSecretRepository.save(userSecretEntity);
    }

    public void delete(@NonNull final UserSecretEntity userSecretEntity) {
        this.userSecretRepository.delete(userSecretEntity);
    }

    public @Nullable String getSecretTOTP(@NonNull final Name id) {
        @Nullable val userSecretEntity = this.findById(id);

        if (userSecretEntity != null)
            return userSecretEntity.getSecretTOTP();

        return null;
    }

    public @Nullable String getSecretTOTP(@NonNull final String login) {
        @Nullable val userSecretEntity = this.findById(login);

        if (userSecretEntity != null)
            return userSecretEntity.getSecretTOTP();

        return null;
    }

    public boolean hasSecretTOTP(@NonNull final Name id) {
        @Nullable val secret = this.getSecretTOTP(id);

        return secret != null ? Base64.isBase64(secret) : false;
    }

    public boolean hasSecretTOTP(@NonNull final String login) {
        @Nullable val secret = this.getSecretTOTP(login);

        return secret != null ? Base64.isBase64(secret) : false;
    }

    @SneakyThrows
    public void setSecretTOTP(@NonNull final Name id, @NonNull final String secretTOTP) {
        @NonNull val userSecretEntity = this.findById(id);
        @NonNull val login = userSecretEntity.getLogin();

        userSecretEntity.setSecretTOTP(secretTOTP);
        this.save(userSecretEntity);
        this.log.info("Secret TOTP {} set for user {}", secretTOTP, login);
    }

    @SneakyThrows
    public void removeSecretTOTP(@NonNull final Name id) {
        @NonNull val userSecretEntity = this.findById(id);
        @NonNull val login = userSecretEntity.getLogin();

        this.delete(userSecretEntity);
        this.log.info("Secret TOTP removed for user {}", login);
    }
}