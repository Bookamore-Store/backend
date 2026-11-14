package com.bookamore.backend.service;

import com.bookamore.backend.dto.offer.OfferFilterRequest;
import com.bookamore.backend.dto.offer.OfferResponse;
import com.bookamore.backend.dto.offer.OfferUpdateRequest;
import com.bookamore.backend.dto.offer.OfferWithBookRequest;
import com.bookamore.backend.dto.offer.OfferWithBookResponse;
import com.bookamore.backend.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface OfferService {

    OfferWithBookResponse create(OfferWithBookRequest request);

    Page<OfferWithBookResponse> getOffersWithBooksPage(OfferFilterRequest filter, Pageable pageable);

    Offer getEntityById(UUID offerId);

    OfferWithBookResponse getWithBookById(UUID offerId);

    OfferResponse update(UUID offerId, OfferUpdateRequest request);

    void delete(UUID offerId);

}
