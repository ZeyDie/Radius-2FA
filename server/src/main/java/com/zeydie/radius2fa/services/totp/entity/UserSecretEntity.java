package com.zeydie.radius2fa.services.totp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.naming.Name;
import java.util.Objects;

import static com.zeydie.radius2fa.services.ldap.config.LdapConfig.*;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users_secrets")
public class UserSecretEntity {
    @Id
    private Name id;

    @Column(name = LOGIN_ATTRIBUTE, nullable = false)
    private String login;
    @Column(name = EMAIL_ATTRIBUTE, nullable = false)
    private String email;
    @Column(name = SECRET_TOTP_ATTRIBUTE, nullable = true)
    private String secretTOTP;

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final UserSecretEntity that = (UserSecretEntity) object;
        return Objects.equals(login, that.login) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, email);
    }
}