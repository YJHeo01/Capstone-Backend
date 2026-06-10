package com.example.orderserver.exception;

public class RobotUnavailableException extends RuntimeException {

    public RobotUnavailableException(String message) {
        super(message);
    }
}
