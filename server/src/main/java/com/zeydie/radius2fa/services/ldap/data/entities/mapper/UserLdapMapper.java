package com.zeydie.radius2fa.services.ldap.data.entities.mapper;

import com.zeydie.radius2fa.services.ldap.data.entities.UserLdapEntity;
import com.zeydie.radius2fa.services.totp.entity.UserSecretEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserLdapMapper {
    @NotNull UserLdapMapper INSTANCE = Mappers.getMapper(UserLdapMapper.class);

    @Nullable UserSecretEntity toUserSecretEntity(@Nullable final UserLdapEntity userLdapEntity);
}