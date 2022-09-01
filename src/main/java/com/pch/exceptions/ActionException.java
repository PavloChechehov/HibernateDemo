package com.pch.exceptions;

public class ActionException extends RuntimeException {
    public ActionException(String message, Exception e) {
        super(message, e);
    }

    public ActionException(String message) {
        super(message);
    }
}
