package com.bookamore.backend.dto.chat;

import com.bookamore.backend.entity.enums.ChatParticipantRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantResponse {
    private UUID id;
    private String name;
    private String avatarUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ChatParticipantRole role;
}
