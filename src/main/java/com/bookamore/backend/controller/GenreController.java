package com.bookamore.backend.controller;

import com.bookamore.backend.annotation.No400Swgr;
import com.bookamore.backend.annotation.No401Swgr;
import com.bookamore.backend.annotation.No404Swgr;
import com.bookamore.backend.dto.genre.GenreResponse;
import com.bookamore.backend.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "Get supported book genre IDs")
    @No400Swgr
    @No401Swgr
    @No404Swgr
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getGenres(
            @Parameter(description = "Filter genres by books in active offers. true: genres with books; " +
                    "false: genres without books; omitted: all genres.")
            @RequestParam(name = "haveBooks", required = false) Boolean haveBooks) {
        if (haveBooks == null) {
            return genreService.getGenres();
        }
        return genreService.getGenresByOffersAvailability(haveBooks);
    }

    @Operation(summary = "Get book genre by ID",
            description = "Returns the genre ID with English and Ukrainian names.")
    @No400Swgr
    @No401Swgr
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenreResponse> getGenreById(@PathVariable("id") String id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @Operation(summary = "Get all supported book genres",
            description = "Returns genre IDs with English and Ukrainian names.")
    @No400Swgr
    @No401Swgr
    @No404Swgr
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GenreResponse> getAllGenres(
            @Parameter(description = "Filter genres by books in active offers. true: genres with books; " +
                    "false: genres without books; omitted: all genres.")
            @RequestParam(name = "haveBooks", required = false) Boolean haveBooks) {
        if (haveBooks == null) {
            return genreService.getGenresFullInfo();
        }
        return genreService.getGenresFullInfoByOffersAvailability(haveBooks);
    }
}
