package com.cs203.smucode.mappers;

import com.cs203.smucode.models.User;
import com.cs203.smucode.models.UserDTO;
import com.cs203.smucode.models.UserIdentificationDTO;
import com.cs203.smucode.models.UserInfoDTO;
import com.cs203.smucode.models.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserInfoDTO userProfileToUserInfoDTO(UserProfile userProfile);

    UserProfile userIdentificationDTOtoUserProfile(UserIdentificationDTO userIdentificationDTO);

}
