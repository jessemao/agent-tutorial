package com.acme.training.platform.error;

/** Shared error contract; business modules provide stable codes and readable messages. */
public class PlatformException extends RuntimeException {

    private final String code;

    public PlatformException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
