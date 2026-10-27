-- liquibase formatted sql

-- changeset Professional:008-normalize-genre-names dbms:postgresql runInTransaction:true
-- Genre names store IDs, not translated labels. Freeze the catalogue at migration time.
-- Prevent concurrent writes while merging names and their book associations.
LOCK TABLE genres, books_genres IN SHARE ROW EXCLUSIVE MODE;

CREATE TEMPORARY TABLE genre_name_migration ON COMMIT DROP AS
WITH supported_genres(name) AS (
    VALUES
        ('biography'),
        ('business_and_entrepreneurship'),
        ('military_studies'),
        ('mystery'),
        ('childrens_books'),
        ('home_housekeeping_gardening'),
        ('reference_books_and_encyclopedias'),
        ('drama'),
        ('economics'),
        ('horror'),
        ('health_and_wellness'),
        ('foreign_languages'),
        ('information_technology_and_programming'),
        ('historical_fiction'),
        ('history_and_archaeology'),
        ('comedy'),
        ('comics_and_manga'),
        ('beauty_fashion_style'),
        ('cooking'),
        ('literary_studies_and_linguistics'),
        ('marketing_advertising_sales'),
        ('mathematics_and_statistics'),
        ('medicine_and_pharmacy'),
        ('management_and_leadership'),
        ('art_design_architecture'),
        ('myths_legends_folklore'),
        ('music_film_theatre'),
        ('educational_books_and_textbooks'),
        ('science_fiction'),
        ('popular_science'),
        ('education_and_parenting'),
        ('young_adult_literature'),
        ('travel_and_geography'),
        ('poetry'),
        ('politics_and_international_relations'),
        ('law'),
        ('adventure'),
        ('nature_and_ecology'),
        ('natural_sciences'),
        ('psychology'),
        ('religion_spirituality_esotericism'),
        ('romance'),
        ('personal_development_and_motivation'),
        ('agriculture'),
        ('family_and_relationships'),
        ('sociology_and_cultural_studies'),
        ('sports_and_outdoor_recreation'),
        ('animals_and_animal_care'),
        ('technology_and_engineering'),
        ('transportation'),
        ('thrillers'),
        ('fantasy'),
        ('philosophy'),
        ('finance_and_investing'),
        ('photography_and_videography'),
        ('hobbies_and_crafts'),
        ('other')
), legacy_genres(old_name, new_name) AS (
    -- Lowercase aliases cover both keys and English values from the old categories.
    -- Names already present in supported_genres need no separate alias.
    VALUES
        ('sci-fi', 'science_fiction'),
        ('romantic', 'romance'),
        ('historical', 'historical_fiction'),
        ('detective', 'mystery'),
        ('children', 'childrens_books')
), normalized AS (
    SELECT genre.id,
           genre.name,
           COALESCE(supported.name, legacy.new_name, 'other') AS target_name
    FROM genres AS genre
    LEFT JOIN supported_genres AS supported ON supported.name = LOWER(genre.name)
    LEFT JOIN legacy_genres AS legacy ON legacy.old_name = LOWER(genre.name)
)
SELECT id AS source_id,
       target_name,
       FIRST_VALUE(id) OVER (
           PARTITION BY target_name
           -- Keep an already valid row; otherwise rename one existing row.
           ORDER BY CASE WHEN name = target_name THEN 0 ELSE 1 END, id
       ) AS target_id
FROM normalized;

-- Several old names can resolve to one unique name, especially 'other'.
-- Copy their links first, excluding links the surviving genre already has.
INSERT INTO books_genres (book_id, genre_id)
SELECT DISTINCT link.book_id, mapping.target_id
FROM books_genres AS link
JOIN genre_name_migration AS mapping ON mapping.source_id = link.genre_id
WHERE mapping.source_id <> mapping.target_id
  AND NOT EXISTS (
      SELECT 1
      FROM books_genres AS existing
      WHERE existing.book_id = link.book_id
        AND existing.genre_id = mapping.target_id
  );

DELETE FROM books_genres
WHERE genre_id IN (
    SELECT source_id FROM genre_name_migration WHERE source_id <> target_id
);

DELETE FROM genres
WHERE id IN (
    SELECT source_id FROM genre_name_migration WHERE source_id <> target_id
);

UPDATE genres AS genre
SET name = mapping.target_name,
    last_modified_date = CURRENT_TIMESTAMP
FROM genre_name_migration AS mapping
WHERE genre.id = mapping.source_id
  AND mapping.source_id = mapping.target_id
  AND genre.name <> mapping.target_name;

-- Merged names cannot be reconstructed automatically; no rollback is provided.
