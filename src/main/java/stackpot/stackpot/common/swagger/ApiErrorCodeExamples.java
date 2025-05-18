package stackpot.stackpot.common.swagger;

import stackpot.stackpot.apiPayload.code.status.ErrorStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExamples {

    ErrorStatus[] value();
}
