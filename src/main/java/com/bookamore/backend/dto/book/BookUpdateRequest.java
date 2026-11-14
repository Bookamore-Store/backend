package com.bookamore.backend.dto.book;

import com.bookamore.backend.annotation.SupportedGenres;
import com.bookamore.backend.entity.enums.BookCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.ISBN;

import java.util.List;
import java.util.UUID;

@Data
public class BookUpdateRequest {

    @Size(min = 1, max = 255, message = "The title must be between 1 and 255 characters.")
    @Schema(example = "Clean Code", description = "Book title")
    private String title;

    @Min(value = 1450, message = "Year of release must be no earlier than 1450.")
    @Max(value = 2100, message = "Year of release must be no later than 2100.")
    @Schema(example = "2008", description = "Year the book was released")
    private Integer yearOfRelease;

    @Size(max = 500, message = "Description can be up to 500 characters.")
    @Schema(example = "A Handbook of Agile Software Craftsmanship.", description = "Book description")
    private String description;

    @ISBN(type = ISBN.Type.ANY, message = "ISBN must be a valid ISBN-10 or ISBN-13.")
    @Schema(example = "9783161484100", description = "Valid ISBN-10 or ISBN-13, with or without hyphens")
    private String isbn;

    @Schema(example = "NEW", description = "Condition of the book")
    private BookCondition condition;

    @Size(min = 1, message = "Authors list cannot be empty.")
    @Schema(example = "[\"Robert C. Martin\"]", description = "List of authors")
    private List<String> authors;

    @SupportedGenres
    @Size(min = 1, message = "Genres list cannot be empty.")
    @Schema(example = "[\"mystery\", \"science_fiction\"]", description = "Supported genre IDs from /api/v1/genres")
    private List<String> genres;

    @Schema(
            description = "Image IDs of this book in the desired order. Listed images get order_index 0, 1, 2, and so on. Omitted images keep their relative order and follow the listed ones.",
            example = "[\"018d4f1a-5b03-71d4-c001-000000000001\", \"018d4f1a-5b03-71d4-c001-000000000002\"]"
    )
    private List<@NotNull(message = "Image id cannot be null.") UUID> imageOrder;
}
