package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.book.BookRequest;
import com.bookamore.backend.dto.offer.OfferFilterRequest;
import com.bookamore.backend.dto.offer.OfferResponse;
import com.bookamore.backend.dto.offer.OfferUpdateRequest;
import com.bookamore.backend.dto.offer.OfferWithBookRequest;
import com.bookamore.backend.dto.offer.OfferWithBookResponse;
import com.bookamore.backend.entity.Book;
import com.bookamore.backend.entity.Offer;
import com.bookamore.backend.entity.enums.OfferStatus;
import com.bookamore.backend.entity.enums.OfferType;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.mapper.offer.OfferMapper;
import com.bookamore.backend.repository.OfferRepository;
import com.bookamore.backend.repository.spec.OfferSpecificationGenerator;
import com.bookamore.backend.service.AccessCheckService;
import com.bookamore.backend.service.BookService;
import com.bookamore.backend.service.OfferFavoriteCounter;
import com.bookamore.backend.service.OfferFavoriteMarker;
import com.bookamore.backend.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;

    private final BookService bookService;

    private final OfferMapper offerMapper;
    private final OfferFavoriteMarker offerFavoriteMarker;
    private final OfferFavoriteCounter offerFavoriteCounter;
    private final AccessCheckService accessCheckService;

    @Transactional
    public OfferWithBookResponse create(OfferWithBookRequest request) {
        BookRequest bookRequest = request.getBook();
        Book savedBook = bookService.createBook(bookRequest);

        Offer offer = offerMapper.toEntity(request);
        offer.setBook(savedBook);
        offer = offerRepository.save(offer);

        OfferWithBookResponse resp = offerMapper.toResponseWithBook(offer);
        resp.setFavorite(false);
        resp.setFavoritesCount(0L);
        return resp;
    }

    @Transactional(readOnly = true)
    public Page<OfferWithBookResponse> getOffersWithBooksPage(OfferFilterRequest filter, Pageable pageable) {
        Page<OfferWithBookResponse> page = getOffersEntityPage(filter, pageable).map(offerMapper::toResponseWithBook);
        offerFavoriteMarker.markWithBook(page);
        offerFavoriteCounter.countWithBook(page);
        return page;
    }

    private Page<Offer> getOffersEntityPage(OfferFilterRequest filter, Pageable pageable) {
        Specification<Offer> spec = OfferSpecificationGenerator.getSpec(filter, pageable.getSort());
        Pageable queryPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Offer> page = offerRepository.findAll(spec, queryPageable);
        return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
    }

    public Offer getEntityById(UUID offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));
    }

    public OfferWithBookResponse getWithBookById(UUID offerId) {
        OfferWithBookResponse response = offerMapper.toResponseWithBook(getEntityById(offerId));
        offerFavoriteMarker.markWithBook(response);
        offerFavoriteCounter.countWithBook(response);
        return response;
    }

    @Transactional
    public OfferResponse update(UUID offerId, OfferUpdateRequest request) {

        Offer existingOffer = offerRepository.findById(offerId).orElseThrow(
                () -> new ResourceNotFoundException("Offer not found with id: " + offerId)
        );
        accessCheckService.requireOfferAuthor(existingOffer);

        Offer patch = offerMapper.toEntity(request);

        boolean isModified = updateSimpleFields(existingOffer, patch);

        if (isModified) {
            Offer savedOffer = offerRepository.save(existingOffer);  // save modified offer
            OfferResponse response = offerMapper.toResponse(savedOffer);
            offerFavoriteMarker.mark(response);
            offerFavoriteCounter.count(response);
            return response;
        }
        OfferResponse response = offerMapper.toResponse(existingOffer);
        offerFavoriteMarker.mark(response);
        offerFavoriteCounter.count(response);
        return response;
    }

    private boolean updateSimpleFields(Offer existingOffer, Offer patch) {
        // Patch Fields
        String patchDescription = patch.getDescription();
        BigDecimal patchPrice = patch.getPrice();
        OfferType patchType = patch.getType();
        OfferStatus patchStatus = patch.getStatus();

        // Fields to update
        String description = existingOffer.getDescription();
        BigDecimal price = existingOffer.getPrice();
        OfferType type = existingOffer.getType();
        OfferStatus status = existingOffer.getStatus();

        boolean isModified = false;

        if (patchDescription != null && !patchDescription.equals(description)) {
            existingOffer.setDescription(patchDescription);
            isModified = true;
        }

        if (patchPrice != null && !patchPrice.equals(price)) {
            existingOffer.setPrice(patchPrice);
            isModified = true;
        }

        if (patchType != null && !patchType.equals(type)) {
            existingOffer.setType(patchType);
            isModified = true;
        }

        if (patchStatus != null && !patchStatus.equals(status)) {
            existingOffer.setStatus(patchStatus);
            isModified = true;
        }

        return isModified;
    }

    @Transactional
    public void delete(UUID offerId) {
        Offer offer = offerRepository.findById(offerId).orElseThrow(
                () -> new ResourceNotFoundException("Offer not found with id: " + offerId)
        );
        accessCheckService.requireOfferAuthor(offer);
        offerRepository.delete(offer);
    }

}
