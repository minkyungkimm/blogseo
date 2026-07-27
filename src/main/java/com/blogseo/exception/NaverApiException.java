package com.blogseo.exception;

public class NaverApiException extends RuntimeException {

    private final int statusCode;

    public NaverApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
