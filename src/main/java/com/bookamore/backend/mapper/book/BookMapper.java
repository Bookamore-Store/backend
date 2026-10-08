package com.bookamore.backend.mapper.book;

import com.bookamore.backend.dto.book.BookRequest;
import com.bookamore.backend.dto.book.BookResponse;
import com.bookamore.backend.dto.book.BookUpdateRequest;
import com.bookamore.backend.entity.Book;
import com.bookamore.backend.mapper.image.ImageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                BookGenreMapper.class, ImageMapper.class
        }
)
public interface BookMapper {
    @Mapping(target = "authors", source = "authorName", qualifiedByName = "authorNameToAuthors")
    BookResponse toResponse(Book book);

    @Mapping(target = "authorName", source = "authors", qualifiedByName = "authorsToAuthorName")
    Book toEntity(BookRequest bookRequest);

    @Mapping(target = "authorName", source = "authors", qualifiedByName = "authorsToAuthorName")
    Book toEntity(BookUpdateRequest bookUpdateRequest);

    @Named("authorsToAuthorName")
    default String authorsToAuthorName(List<String> authors) {
        return authors == null ? null : String.join(", ", authors);
    }

    @Named("authorNameToAuthors")
    default List<String> authorNameToAuthors(String authorName) {
        return authorName == null ? List.of() : List.of(authorName);
    }
}
