package com.bookamore.backend.dto.chat;

import com.bookamore.backend.entity.enums.BookCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBookSummaryResponse {
    private UUID id;
    private String title;
    private BookCondition condition;
}
