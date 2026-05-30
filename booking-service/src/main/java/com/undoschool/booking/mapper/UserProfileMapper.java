package com.undoschool.booking.mapper;

import com.undoschool.booking.domain.UserProfile;
import com.undoschool.booking.dto.ProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    ProfileResponse toResponse(UserProfile profile);
}
