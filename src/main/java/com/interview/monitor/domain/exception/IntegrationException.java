package com.interview.monitor.domain.exception;


public class IntegrationException extends RuntimeException {
    public IntegrationException(Exception ex) {
        super(ex);
    }

    public IntegrationException(String msg) {
        super(msg);
    }

    public IntegrationException(String msg, Exception ex) {
        super(msg, ex);
    }
}