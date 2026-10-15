package com.bookamore.backend.validation;

import com.bookamore.backend.annotation.SupportedGenres;
import com.bookamore.backend.service.GenreService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SupportedGenresValidator implements ConstraintValidator<SupportedGenres, List<String>> {

    private final GenreService genreService;

    @Override
    public boolean isValid(List<String> genres, ConstraintValidatorContext context) {
        if (genres == null) {
            return true;
        }
        List<String> supportedGenres = genreService.getGenres();
        return genres.stream().allMatch(genre -> genre != null && supportedGenres.contains(genre));
    }
}
