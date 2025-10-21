package io.hhplus.tdd;

import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.hhplus.tdd.point.exception.PointException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse(
            "500",
            "에러가 발생했습니다.",
            Collections.emptyList()
        ));
    }

    @ExceptionHandler(PointException.class)
    public ResponseEntity<ErrorResponse> handlePoint(PointException e){
        HttpStatus status = switch (e.getCode()) {
            case NEGATIVE_CHARGE -> HttpStatus.BAD_REQUEST;
            case NOT_ENOUGH_POINT -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(new ErrorResponse(
            e.getCode().name(),
            e.getMessage(),
            Collections.emptyList()
        ));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex){
        List<ErrorResponse.ErrorDetail> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toDetail)
            .collect(toList());

        ErrorResponse body = new ErrorResponse(
            "INVALID_PARAMETER",
            "요청 본문이 유효하지 않습니다.",
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex){
        List<ErrorResponse.ErrorDetail> details = ex.getConstraintViolations()
            .stream()
            .map(this::toDetail)
            .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponse(
            "INVALID_PARAMETER",
            "요청 파라미터가 유효하지 않습니다.",
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ErrorResponse.ErrorDetail toDetail(FieldError fe) {
        return new ErrorResponse.ErrorDetail(
            fe.getField(),
            fe.getDefaultMessage(),
            fe.getRejectedValue()
        );
    }
    private ErrorResponse.ErrorDetail toDetail(ConstraintViolation<?> v) {
        return new ErrorResponse.ErrorDetail(
            v.getPropertyPath().toString(),
            v.getMessage(),
            v.getInvalidValue()
        );
    }

}
