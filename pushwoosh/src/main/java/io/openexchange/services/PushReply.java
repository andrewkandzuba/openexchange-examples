package io.openexchange.services;

public class PushReply extends Reply {
    private final String messageId;

    public PushReply(int code, String status, String messageId) {
        super(code, status);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
