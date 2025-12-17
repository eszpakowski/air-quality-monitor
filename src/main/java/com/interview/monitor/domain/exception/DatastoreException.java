package com.interview.monitor.domain.exception;


public class DatastoreException extends RuntimeException {
    public DatastoreException(Exception ex) {
        super(ex);
    }

    public DatastoreException(String msg) {
        super(msg);
    }

    public DatastoreException(String msg, Exception ex) {
        super(msg, ex);
    }
}