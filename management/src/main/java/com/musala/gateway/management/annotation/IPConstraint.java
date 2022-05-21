package com.musala.gateway.management.annotation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;



@SuppressWarnings("unused")
@Constraint(validatedBy = IPValidator.class)
@Target( {ElementType.METHOD, ElementType.FIELD })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface IPConstraint {
    String message() default "Invalid IP address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
