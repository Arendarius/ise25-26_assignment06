package de.seuhd.campuscoffee.api.mapper;

import de.seuhd.campuscoffee.api.dtos.PosDto;
import de.seuhd.campuscoffee.api.dtos.UserDto;

@Mapper(componentModel = "spring")
@ConditionalOnMissingBean // prevent IntelliJ warning about duplicate bean
public interface UserDtoMapper {
    //TODO: Implement user DTO mapper
    UserDto fromDomain(UserDto source);
    UserDto toDomain(UserDto source);
}