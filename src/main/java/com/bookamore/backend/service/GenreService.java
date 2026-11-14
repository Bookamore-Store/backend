package com.bookamore.backend.service;

import com.bookamore.backend.dto.genre.GenreResponse;

import java.util.List;

public interface GenreService {

    List<String> getGenres();

    List<String> getGenresByOffersAvailability(boolean haveBooks);

    GenreResponse getGenreById(String id);

    List<GenreResponse> getGenresFullInfo();

    List<GenreResponse> getGenresFullInfoByOffersAvailability(boolean haveBooks);
}
