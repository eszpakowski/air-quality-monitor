package com.interview.monitor.domain.exception;


public class ValidationException extends RuntimeException {
    public ValidationException(Exception ex) {
        super(ex);
    }

    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(String msg, Exception ex) {
        super(msg, ex);
    }
}