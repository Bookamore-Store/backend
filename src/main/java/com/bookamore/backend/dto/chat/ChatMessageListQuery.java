package com.bookamore.backend.dto.chat;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageListQuery {

    @Parameter(description = "Return messages newer than this id (cannot be used with `before`)")
    private UUID after;

    @Parameter(description = "Return messages older than this id (cannot be used with `after`)")
    private UUID before;

    @Parameter(description = "Max messages to return (default 50, max 100)")
    private Integer limit;

    @AssertTrue(message = "after and before cannot be used together")
    @Schema(hidden = true)
    public boolean isCursorsExclusive() {
        return after == null || before == null;
    }
}
