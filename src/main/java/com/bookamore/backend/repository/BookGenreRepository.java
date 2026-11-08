package com.bookamore.backend.repository;

import com.bookamore.backend.entity.BookGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BookGenreRepository extends JpaRepository<BookGenre, UUID> {
     Optional<BookGenre> findByName(String name);

     @Query("select distinct genre from BookGenre genre join genre.books book join book.offer offer " +
             "where offer.status = com.bookamore.backend.entity.enums.OfferStatus.OPEN")
     Set<BookGenre> findAllThatHaveAtLeastOneBook();
}
