package com.bookamore.backend.mapper.offer;

import com.bookamore.backend.dto.offer.OfferResponse;
import com.bookamore.backend.dto.offer.OfferUpdateRequest;
import com.bookamore.backend.dto.offer.OfferWithBookRequest;
import com.bookamore.backend.dto.offer.OfferWithBookResponse;
import com.bookamore.backend.dto.offer.Seller;
import com.bookamore.backend.entity.Offer;
import com.bookamore.backend.entity.User;
import com.bookamore.backend.mapper.book.BookMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = BookMapper.class
)
public interface OfferMapper {

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "seller", source = "user")
    @Mapping(target = "favorite", ignore = true)
    OfferResponse toResponse(Offer offer);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Seller toSeller(User user); // Helper method for mapping user to Seller DTO

    @Mapping(target = "book", source = "book")
    @Mapping(target = "seller", source = "user")
    @Mapping(target = "favorite", ignore = true)
    OfferWithBookResponse toResponseWithBook(Offer offer);

    @Mapping(target = "book", source = "request")
    @Mapping(target = "user", source = "sellerId", qualifiedByName = "createUserFromId")
    Offer toEntity(OfferWithBookRequest request);

    Offer toEntity(OfferUpdateRequest request);

    @Named("createUserFromId")
    default User createUserFromId(UUID id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
