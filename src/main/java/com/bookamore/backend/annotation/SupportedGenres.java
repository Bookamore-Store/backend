package com.bookamore.backend.annotation;

import com.bookamore.backend.validation.SupportedGenresValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = SupportedGenresValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface SupportedGenres {

    String message() default "Genres must contain only supported genre IDs from /api/v1/genres.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
