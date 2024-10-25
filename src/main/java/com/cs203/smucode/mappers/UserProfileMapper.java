package com.cs203.smucode.mappers;

import com.cs203.smucode.dto.UserIdentificationDTO;
import com.cs203.smucode.dto.UserInfoDTO;
import com.cs203.smucode.models.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);

    UserInfoDTO userProfileToUserInfoDTO(UserProfile userProfile);

    UserProfile userIdentificationDTOtoUserProfile(UserIdentificationDTO userIdentificationDTO);

}
