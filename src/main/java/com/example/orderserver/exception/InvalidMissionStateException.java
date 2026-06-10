package com.example.orderserver.exception;

public class InvalidMissionStateException extends RuntimeException {

    public InvalidMissionStateException(String message) {
        super(message);
    }
}
