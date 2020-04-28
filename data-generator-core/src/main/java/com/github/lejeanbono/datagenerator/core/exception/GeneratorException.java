package com.github.lejeanbono.datagenerator.core.exception;

public class GeneratorException extends RuntimeException {

    public GeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneratorException(String message) {
        super(message);
    }
}
