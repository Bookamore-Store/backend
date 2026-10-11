-- liquibase formatted sql

-- changeset Professional:007-replace-book-authors-with-author-name dbms:postgresql
ALTER TABLE books ADD COLUMN author_name TEXT;

UPDATE books AS book
SET author_name = names.author_name
FROM (
    SELECT book_author.book_id,
           string_agg(DISTINCT author.name, ', ' ORDER BY author.name) AS author_name
    FROM books_authors AS book_author
    JOIN authors AS author ON author.id = book_author.author_id
    GROUP BY book_author.book_id
) AS names
WHERE book.id = names.book_id;

DROP TABLE books_authors;
DROP TABLE authors;
