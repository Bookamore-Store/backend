package com.bookamore.backend.dto.offer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Inclusive price range. Either bound may be omitted.")
public class PriceRange {

    @DecimalMin(value = "0.0", inclusive = true, message = "Price from must be 0 or greater.")
    @Schema(example = "50.00", description = "Minimum price (inclusive)")
    private BigDecimal from;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price to must be 0 or greater.")
    @Schema(example = "300.00", description = "Maximum price (inclusive)")
    private BigDecimal to;

    @AssertTrue(message = "Price from must not be greater than price to.")
    @Schema(hidden = true)
    public boolean isFromNotGreaterThanTo() {
        return from == null || to == null || from.compareTo(to) <= 0;
    }
}
