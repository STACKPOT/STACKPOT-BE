package stackpot.stackpot.Validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import stackpot.stackpot.Validation.validator.RoleValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoleValidator.class)
@Documented
public @interface ValidRole {
    String message() default "유효하지 않은 모집 역할입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
