package io.openexchange.domain;

public enum Type {
    IOS(1);

    private int code;

    Type(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}