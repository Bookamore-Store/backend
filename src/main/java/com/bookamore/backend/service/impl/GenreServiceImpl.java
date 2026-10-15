package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.genre.GenreResponse;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.service.GenreService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GenreServiceImpl implements GenreService {

    private final List<GenreResponse> genres;
    private final List<String> genreIds;
    private final Map<String, GenreResponse> genresById;

    public GenreServiceImpl(ObjectMapper objectMapper) throws IOException {
        try (InputStream input = new ClassPathResource("book-genres.json").getInputStream()) {
            genres = List.copyOf(objectMapper.readValue(input, new TypeReference<List<GenreResponse>>() {}));
        }
        genreIds = genres.stream().map(GenreResponse::id).toList();
        Map<String, GenreResponse> index = new HashMap<>();
        for (GenreResponse genre : genres) {
            index.putIfAbsent(genre.id(), genre);
        }
        genresById = Collections.unmodifiableMap(index);
    }

    @Override
    public List<String> getGenres() {
        return genreIds;
    }

    @Override
    public GenreResponse getGenreById(String id) {
        return Optional.ofNullable(genresById.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        return genres;
    }
}
