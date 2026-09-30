package com.bookamore.backend.dto.book;

import com.bookamore.backend.dto.image.ImageShortResponse;
import com.bookamore.backend.entity.enums.BookCondition;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private UUID id;
    private String title;
    private Integer yearOfRelease;
    private String description;
    private String isbn;
    private BookCondition condition;
    private List<String> authors;
    private List<String> genres;
    private List<ImageShortResponse> images;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
}
