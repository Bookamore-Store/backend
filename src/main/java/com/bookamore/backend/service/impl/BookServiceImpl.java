package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.book.BookRequest;
import com.bookamore.backend.dto.book.BookResponse;
import com.bookamore.backend.dto.book.BookUpdateRequest;
import com.bookamore.backend.entity.Book;
import com.bookamore.backend.entity.BookGenre;
import com.bookamore.backend.entity.enums.BookCondition;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.mapper.book.BookMapper;
import com.bookamore.backend.repository.BookGenreRepository;
import com.bookamore.backend.repository.BookRepository;
import com.bookamore.backend.service.AccessCheckService;
import com.bookamore.backend.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookGenreRepository bookGenreRepository;
    private final BookMapper bookMapper;
    private final AccessCheckService accessCheckService;

    @Transactional
    public Book createBook(BookRequest bookRequest) {
        Book book = bookMapper.toEntity(bookRequest);

        resolveReferences(book);

        return bookRepository.save(book);
    }

    @Transactional
    public BookResponse create(BookRequest bookRequest) {
        return bookMapper.toResponse(createBook(bookRequest));
    }

    private List<BookGenre> resolveGenres(Book book) {
        List<BookGenre> managedGenres = new ArrayList<>();
        for (BookGenre genre : book.getGenres()) {
            String genreName = genre.getName();
            bookGenreRepository.findByName(genreName).ifPresentOrElse(
                    existedGenre -> {
                        existedGenre.getBooks().add(book);
                        managedGenres.add(existedGenre);
                    },
                    () -> {
                        // new Genre
                        managedGenres.add(
                                bookGenreRepository.save(genre)
                        );
                    }
            );
        }
        return managedGenres;
    }

    private void resolveReferences(Book book) {
        book.setGenres(resolveGenres(book));
    }

    public Book getBookEntityById(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    public BookResponse getById(UUID bookId) {
        return bookMapper.toResponse(
                getBookEntityById(bookId)
        );
    }

    @Transactional
    public BookResponse update(UUID bookId, BookUpdateRequest bookUpdateRequest) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        accessCheckService.requireOfferAuthor(existingBook.getOffer());

        Book patch = bookMapper.toEntity(bookUpdateRequest);


        boolean simpleFieldsModified = updateSimpleFields(existingBook, patch);

        boolean childrenModified = false;

        childrenModified |= updateGenres(existingBook, patch);

        boolean anyModified = childrenModified | simpleFieldsModified;

        if (anyModified) {
            if (childrenModified) {
                // Update lastModifiedDate only when child entities are modified
                existingBook.setLastModifiedDate(LocalDateTime.now());
            }
            Book savedBook = bookRepository.save(existingBook);  // save modified book
            return bookMapper.toResponse(savedBook);
        }

        return bookMapper.toResponse(existingBook);
    }

    private boolean updateSimpleFields(Book existingBook, Book patch) {
        boolean isModified = false;

        String newTitle = patch.getTitle();
        String newAuthorName = patch.getAuthorName();
        String newDesc = patch.getDescription();
        String newIsbn = patch.getIsbn();
        BookCondition newCondition = patch.getCondition();

        if (newTitle != null && !newTitle.equals(existingBook.getTitle())) {
            existingBook.setTitle(newTitle);
            isModified = true;
        }

        if (newAuthorName != null && !newAuthorName.equals(existingBook.getAuthorName())) {
            existingBook.setAuthorName(newAuthorName);
            isModified = true;
        }
        if (newDesc != null && !newDesc.equals(existingBook.getDescription())) {
            existingBook.setDescription(newDesc);
            isModified = true;
        }

        if (newIsbn != null && !newIsbn.equals(existingBook.getIsbn())) {
            existingBook.setIsbn(newIsbn);
            isModified = true;
        }

        if (newCondition != null && !newCondition.equals(existingBook.getCondition())) {
            existingBook.setCondition(newCondition);
            isModified = true;
        }

        return isModified;
    }

    private boolean updateGenres(Book existingBook, Book patch) {

        List<BookGenre> patchGenres = patch.getGenres();
        if (patchGenres == null || patchGenres.equals(existingBook.getGenres())) {
            return false;
        }
        if (patchGenres.stream().anyMatch(g -> g.getName() == null || g.getName().isBlank())) {
            throw new IllegalArgumentException("One or more provided genres has no name "
                    + "(name == null or name is blank)");
        }

        Set<String> patchGenresNames = patchGenres.stream().map(BookGenre::getName).collect(Collectors.toSet());

        List<BookGenre> existingGenres = existingBook.getGenres();
        existingGenres.removeIf(genre -> !patchGenresNames.contains(genre.getName()));  // unassign genres

        Set<String> existingGenresNames = existingGenres.stream()
                .map(BookGenre::getName).collect(Collectors.toSet());

        for (String genreName : patchGenresNames) {
            if (existingGenresNames.contains(genreName)) {
                continue;
            }

            // assign existing genre to book
            bookGenreRepository.findByName(genreName).ifPresentOrElse(
                    existingGenres::add,
                    () -> {
                        BookGenre newGenre = new BookGenre();
                        newGenre.setName(genreName);
                        existingGenres.add(newGenre);  // assign new genre
                        bookGenreRepository.save(newGenre);
                    }
            );
        }

        return true;
    }

}