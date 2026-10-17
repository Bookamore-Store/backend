package com.bookamore.backend.mapper.user;

import com.bookamore.backend.dto.singup.SignUpRequest;
import com.bookamore.backend.dto.singup.SignUpResponse;
import com.bookamore.backend.dto.user.UserResponse;
import com.bookamore.backend.entity.User;
import com.bookamore.backend.mapper.image.ImageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ImageMapper.class)
public interface UserMapper {
    User signUpRequestToUser(SignUpRequest signUpRequest);

    @Mapping(target = "socialAvatarUrl", source = "avatarUrl")
    @Mapping(target = "uploadedImage", source = "images", qualifiedByName = "firstImageToShortResponse")
    UserResponse userToUserResponse(User user);

    @Mapping(target = "message", constant = "Sign up is successful!")
    SignUpResponse userToSignUpResponse(User user);
}
