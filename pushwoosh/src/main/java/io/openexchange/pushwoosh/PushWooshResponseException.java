package io.openexchange.pushwoosh;


public class PushWooshResponseException extends Exception {
    private final int code;
    private final String message;

    public PushWooshResponseException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "PushWooshResponseException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
