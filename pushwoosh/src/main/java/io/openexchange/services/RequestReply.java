package io.openexchange.services;

public class RequestReply extends Reply{
    private final String requestId;

    public RequestReply(int code, String status, String requestId) {
        super(code, status);
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
