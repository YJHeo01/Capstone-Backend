package com.example.orderserver.controller;

import com.example.orderserver.exception.InvalidOrderStatusTransitionException;
import com.example.orderserver.exception.InvalidMissionStateException;
import com.example.orderserver.exception.MissionNotFoundException;
import com.example.orderserver.exception.OrderNotFoundException;
import com.example.orderserver.exception.CampusMapNodeNotFoundException;
import com.example.orderserver.exception.CampusRouteNotFoundException;
import com.example.orderserver.exception.RobotUnavailableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Order not found");
        return detail;
    }

    @ExceptionHandler(MissionNotFoundException.class)
    public ProblemDetail handleMissionNotFound(MissionNotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Mission not found");
        return detail;
    }

    @ExceptionHandler(CampusMapNodeNotFoundException.class)
    public ProblemDetail handleCampusMapNodeNotFound(CampusMapNodeNotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Campus map node not found");
        return detail;
    }

    @ExceptionHandler(CampusRouteNotFoundException.class)
    public ProblemDetail handleCampusRouteNotFound(CampusRouteNotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle("Campus route not found");
        return detail;
    }

    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ProblemDetail handleInvalidStatus(InvalidOrderStatusTransitionException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setTitle("Invalid order status transition");
        return detail;
    }

    @ExceptionHandler(InvalidMissionStateException.class)
    public ProblemDetail handleInvalidMissionState(InvalidMissionStateException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setTitle("Invalid mission state");
        return detail;
    }

    @ExceptionHandler(RobotUnavailableException.class)
    public ProblemDetail handleRobotUnavailable(RobotUnavailableException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("Robot unavailable");
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Validation failed");

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setTitle("Constraint violation");
        return detail;
    }
}
