package com.bookamore.backend.mapper.image;

import com.bookamore.backend.dto.image.ImageRequest;
import com.bookamore.backend.dto.image.ImageResponse;
import com.bookamore.backend.dto.image.ImageShortResponse;
import com.bookamore.backend.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = ImageUrlMapper.class
)
public interface ImageMapper {

    @Mapping(target = "path", source = "path", qualifiedByName = "imageUrl")
    ImageResponse toResponse(Image image);

    @Mapping(target = "path", source = "pathOfSavedFile")
    Image toEntity(ImageRequest imageRequest, String pathOfSavedFile);

    @Mapping(target = "path", source = "path", qualifiedByName = "imageUrl")
    ImageShortResponse toShortResponse(Image image);
}
