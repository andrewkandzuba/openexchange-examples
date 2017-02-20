package io.openexchange.services;

public class Reply {
    private final int code;
    private final String status;

    public Reply(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }
}
