package com.bookamore.backend.dto.offer;

import com.bookamore.backend.entity.enums.BookCondition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
        Offer search filters as query parameters. Repeat a parameter for several values (`genre=A&genre=B`): \
        values within each list are combined with OR. Genre matching ignores case; author and title use case-insensitive substring matching. \
        Different fields are combined with AND. `price` is a from–to range. Year of release is `yearOfRelease`.
        """)
public class OfferFilterRequest {

    @Parameter(description = "Seller id. Example: `sellerId=018d4f1a-5b03-71d4-a716-446655440001&sellerId=018d4f1a-5b03-71d4-a716-446655440002`")
    private List<UUID> sellerId;

    @Parameter(description = "Genre name. Example: `genre=Поезія&genre=Роман`")
    private List<String> genre;

    @Parameter(description = "Book title", example = "Кобзар")
    private String title;

    @Parameter(description = "Author name. Example: `author=Шевченко&author=Хвильовий`")
    private List<String> author;

    @Parameter(description = "Book condition. Example: `condition=NEW&condition=USED`")
    private List<BookCondition> condition;

    @Parameter(description = "ISBN. Example: `isbn=0596520689&isbn=9783161484100`")
    private List<String> isbn;

    @Parameter(description = "Year of release. Example: `yearOfRelease=1840&yearOfRelease=1911`")
    private List<Integer> yearOfRelease;

    @Valid
    @Parameter(description = "Price range from–to")
    private PriceRange price;
}
