package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.genre.GenreResponse;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.repository.BookGenreRepository;
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
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {

    private final List<GenreResponse> genres;
    private final List<String> genreIds;
    private final Map<String, GenreResponse> genresById;
    private final BookGenreRepository bookGenreRepository;

    public GenreServiceImpl(ObjectMapper objectMapper, BookGenreRepository bookGenreRepository) throws IOException {
        this.bookGenreRepository = bookGenreRepository;
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
    public List<String> getGenresByOffersAvailability(boolean haveBooks) {
        Set<String> genresWithActiveOffers = getGenreIdsWithActiveOffers();
        return genreIds.stream()
                .filter(genreId -> genresWithActiveOffers.contains(genreId) == haveBooks)
                .toList();
    }

    private Set<String> getGenreIdsWithActiveOffers() {
        return bookGenreRepository.findAllThatHaveAtLeastOneBook().stream()
                .map(genre -> genre.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Override
    public GenreResponse getGenreById(String id) {
        return Optional.ofNullable(genresById.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
    }

    @Override
    public List<GenreResponse> getGenresFullInfo() {
        return genres;
    }

    @Override
    public List<GenreResponse> getGenresFullInfoByOffersAvailability(boolean haveBooks) {
        Set<String> genresWithActiveOffers = getGenreIdsWithActiveOffers();
        return genres.stream()
            .filter(genre -> genresWithActiveOffers.contains(genre.id()) == haveBooks)
            .toList();
    }
}
