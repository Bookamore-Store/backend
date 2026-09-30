package com.bookamore.backend.service;

import com.bookamore.backend.entity.Offer;

public interface AccessCheckService {

    void requireOfferAuthor(Offer offer);
}
