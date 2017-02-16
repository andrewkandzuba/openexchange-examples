package io.openexchange.domain;

public class Device {
    private final String hwid;
    private final String token;
    private final Type type;

    public Device(String hwid, String token, Type type) {
        this.hwid = hwid;
        this.token = token;
        this.type = type;
    }

    public String getHwid() {
        return hwid;
    }

    public String getToken() {
        return token;
    }

    public Type getType() {
        return type;
    }
}
