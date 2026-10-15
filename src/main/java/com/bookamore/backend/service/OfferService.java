package com.bookamore.backend.service;

import com.bookamore.backend.dto.offer.OfferFilterRequest;
import com.bookamore.backend.dto.offer.OfferRequest;
import com.bookamore.backend.dto.offer.OfferResponse;
import com.bookamore.backend.dto.offer.OfferUpdateRequest;
import com.bookamore.backend.dto.offer.OfferWithBookRequest;
import com.bookamore.backend.dto.offer.OfferWithBookResponse;
import com.bookamore.backend.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface OfferService {

    OfferResponse create(OfferRequest request);

    OfferWithBookResponse create(OfferWithBookRequest request);

    Page<OfferResponse> getOffersPage(OfferFilterRequest filter, Pageable pageable);

    Page<OfferWithBookResponse> getOffersWithBooksPage(OfferFilterRequest filter, Pageable pageable);

    Offer getEntityById(UUID offerId);

    OfferResponse getById(UUID offerId);

    OfferWithBookResponse getWithBookById(UUID offerId);

    OfferResponse update(UUID offerId, OfferUpdateRequest request);

    void delete(UUID offerId);

}
