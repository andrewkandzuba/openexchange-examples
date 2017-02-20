package io.openexchange.services;

import io.openexchange.pushwoosh.PushWooshResponseException;

import java.io.IOException;

public interface Reporter {
    RequestReply getMessageStats(String messageId) throws IOException, PushWooshResponseException;

    RowsReply getResults(String requestId) throws IOException, PushWooshResponseException;
}
