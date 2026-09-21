package com.example.tickettriage.error;

public class AiTriageException extends RuntimeException {

    public AiTriageException(String message, Throwable cause) {
        super(message, cause);
    }
}
