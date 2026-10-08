package com.bookamore.backend.repository.spec;

import com.bookamore.backend.dto.offer.OfferFilterRequest;
import com.bookamore.backend.dto.offer.PriceRange;
import com.bookamore.backend.entity.Book;
import com.bookamore.backend.entity.BookGenre_;
import com.bookamore.backend.entity.Book_;
import com.bookamore.backend.entity.Offer;
import com.bookamore.backend.entity.Offer_;
import com.bookamore.backend.entity.User_;
import com.bookamore.backend.exception.UnsupportedSortFieldException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OfferSpecificationGenerator {

    private static final Set<String> SUPPORTED_BOOK_SORT_FIELDS = Set.of(
            Book_.TITLE, Book_.YEAR_OF_RELEASE, Book_.CONDITION, Book_.AUTHOR_NAME
    );
    private static final Set<String> SUPPORTED_OFFER_SORT_FIELDS = Set.of(
            Offer_.CREATED_DATE, Offer_.LAST_MODIFIED_DATE, Offer_.PRICE, Offer_.TYPE
    );

    public static Specification<Offer> getSpec(OfferFilterRequest filter, Sort sort) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (hasValues(filter.getSellerId())) {
                    predicates.add(root.get(Offer_.USER).get(User_.ID).in(filter.getSellerId()));
                }
                if (hasValues(filter.getGenre())) {
                    predicates.add(matchesGenres(filter.getGenre(), root, query, cb));
                }
                if (hasValues(filter.getAuthor())) {
                    List<String> authors = filter.getAuthor().stream()
                            .filter(name -> name != null && !name.isBlank()).toList();
                    if (!authors.isEmpty()) {
                        predicates.add(matchesAuthors(authors, root, cb));
                    }
                }
                if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get(Offer_.BOOK).get(Book_.TITLE)),
                            "%" + filter.getTitle().toLowerCase(Locale.ROOT) + "%"));
                }
                if (hasValues(filter.getCondition())) {
                    predicates.add(root.get(Offer_.BOOK).get(Book_.CONDITION).in(filter.getCondition()));
                }
                if (hasValues(filter.getIsbn())) {
                    predicates.add(root.get(Offer_.BOOK).get(Book_.ISBN).in(filter.getIsbn()));
                }
                if (hasValues(filter.getYearOfRelease())) {
                    predicates.add(root.get(Offer_.BOOK).get(Book_.YEAR_OF_RELEASE).in(filter.getYearOfRelease()));
                }
                PriceRange price = filter.getPrice();
                if (price != null) {
                    if (price.getFrom() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get(Offer_.PRICE), price.getFrom()));
                    }
                    if (price.getTo() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get(Offer_.PRICE), price.getTo()));
                    }
                }
            }
            assert query != null;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
                for (Sort.Order order : sort) {
                    Expression<?> expression = sortExpression(order, root, cb);
                    orders.add(order.isAscending() ? cb.asc(expression) : cb.desc(expression));
                }
                query.orderBy(orders);
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate matchesGenres(List<String> genres, Root<Offer> root,
                                           CriteriaQuery<?> query, CriteriaBuilder cb) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Join<Book, ?> genre = subquery.correlate(root).<Offer, Book>join(Offer_.BOOK).join(Book_.GENRES);
        subquery.select(cb.literal(1)).where(cb.upper(genre.get(BookGenre_.NAME)).in(
                genres.stream().map(value -> value.toUpperCase(Locale.ROOT)).toList()));
        return cb.exists(subquery);
    }

    private static Predicate matchesAuthors(List<String> authors, Root<Offer> root, CriteriaBuilder cb) {
        Expression<String> authorName = cb.lower(root.get(Offer_.BOOK).get(Book_.AUTHOR_NAME));
        Predicate[] matches = authors.stream()
                .map(name -> cb.like(authorName, "%" + name.toLowerCase(Locale.ROOT) + "%"))
                .toArray(Predicate[]::new);
        return cb.or(matches);
    }

    private static Expression<?> sortExpression(Sort.Order order, Root<Offer> root, CriteriaBuilder cb) {
        String field = order.getProperty();
        if (SUPPORTED_BOOK_SORT_FIELDS.contains(field)) {
            if (field.equals(Book_.TITLE) || field.equals(Book_.AUTHOR_NAME)) {
                return cb.lower(root.get(Offer_.BOOK).get(field));
            }
            return root.get(Offer_.BOOK).get(field);
        }
        if (SUPPORTED_OFFER_SORT_FIELDS.contains(field)) {
            return root.get(field);
        }
        throw new UnsupportedSortFieldException(field);
    }

    private static boolean hasValues(Collection<?> values) {
        return values != null && !values.isEmpty();
    }
}
