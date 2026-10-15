package com.bookamore.backend.service;

import com.bookamore.backend.dto.genre.GenreResponse;

import java.util.List;

public interface GenreService {

    List<String> getGenres();

    GenreResponse getGenreById(String id);

    List<GenreResponse> getAllGenres();
}
