package com.bookamore.backend.service.impl;

import com.bookamore.backend.entity.Offer;
import com.bookamore.backend.exception.ForbiddenAccessException;
import com.bookamore.backend.service.AccessCheckService;
import com.bookamore.backend.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessCheckServiceImpl implements AccessCheckService {

    @Override
    public void requireOfferAuthor(Offer offer) {
        UUID authorId = null;
        if (offer != null && offer.getUser() != null) {
            authorId = offer.getUser().getId();
        }
        if (authorId == null || !authorId.equals(SecurityUtils.getAuthenticatedUserId())) {
            throw new ForbiddenAccessException("Only the author can perform this action");
        }
    }
}
