package com.bookamore.backend.config.openapi;

import com.bookamore.backend.annotation.No400Swgr;
import com.bookamore.backend.annotation.No401Swgr;
import com.bookamore.backend.annotation.No404Swgr;
import com.bookamore.backend.annotation.No409Swgr;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Configuration
public class OpenApiOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (operation == null || operation.getResponses() == null) {
            return operation;
        }

        handleRemoveCodeAnnotations(operation, handlerMethod);
        removeUndeclaredStatus(operation, handlerMethod, HttpStatus.UNPROCESSABLE_ENTITY);

        return operation;
    }

    /**
     * springdoc copies {@code @ResponseStatus} from {@code @ExceptionHandler} onto every operation.
     * Keep that status only where the controller method lists it in {@code @ApiResponse}.
     */
    private void removeUndeclaredStatus(Operation operation, HandlerMethod handlerMethod, HttpStatus status) {
        String code = String.valueOf(status.value());
        if (!declaresResponseCode(handlerMethod.getMethod(), code)) {
            operation.getResponses().remove(code);
        }
    }

    private boolean declaresResponseCode(Method method, String code) {
        for (ApiResponse response : method.getAnnotationsByType(ApiResponse.class)) {
            if (code.equals(response.responseCode())) {
                return true;
            }
        }
        ApiResponses responses = method.getAnnotation(ApiResponses.class);
        if (responses == null) {
            return false;
        }
        for (ApiResponse response : responses.value()) {
            if (code.equals(response.responseCode())) {
                return true;
            }
        }
        return false;
    }

    private void removeByAnnotation(Operation operation, HandlerMethod handlerMethod,
                                    Class<? extends Annotation> annotationClass,
                                    HttpStatus httpStatus) {
        boolean isOnMethod = handlerMethod.getMethod().isAnnotationPresent(annotationClass);
        boolean isOnClass = handlerMethod.getBeanType().isAnnotationPresent(annotationClass);

        if (isOnMethod || isOnClass) {
            String code = String.valueOf(httpStatus.value());
            operation.getResponses().remove(code);
        }
    }

    private void handleRemoveCodeAnnotations(Operation operation, HandlerMethod handlerMethod) {
        removeByAnnotation(operation, handlerMethod, No400Swgr.class, HttpStatus.BAD_REQUEST);
        removeByAnnotation(operation, handlerMethod, No401Swgr.class, HttpStatus.UNAUTHORIZED);
        removeByAnnotation(operation, handlerMethod, No404Swgr.class, HttpStatus.NOT_FOUND);
        removeByAnnotation(operation, handlerMethod, No409Swgr.class, HttpStatus.CONFLICT);
    }
}
