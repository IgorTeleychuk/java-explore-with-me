package ru.practicum.ewmservice.util.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventDateUpdateValidator.class)
public @interface EventDateUpdateValidation {
    String message() default "The start time cannot be earlier than two hours after publication";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
