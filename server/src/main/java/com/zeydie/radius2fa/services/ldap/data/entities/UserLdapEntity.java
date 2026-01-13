package com.zeydie.radius2fa.services.ldap.data.entities;

import lombok.Getter;
import lombok.ToString;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;
import java.util.Objects;

import static com.zeydie.radius2fa.services.ldap.config.LdapConfig.*;

@ToString
@Getter
@Entry(objectClasses = "user")
public final class UserLdapEntity {
    @Id
    private Name id;

    @Attribute(name = LOGIN_ATTRIBUTE)
    private String login;

    @Attribute(name = EMAIL_ATTRIBUTE)
    private String email;

    @Attribute(name = GROUPS_ATTRIBUTE)
    private List<String> groups;

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final UserLdapEntity that = (UserLdapEntity) object;
        return Objects.equals(login, that.login) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, email);
    }
}