package io.openexchange.services;

import io.openexchange.pojos.pushwoosh.Row;
import io.openexchange.pushwoosh.PushWooshResponseException;

import java.io.IOException;
import java.util.List;

public interface Reporter {
    String getMessageStats(String messageId) throws IOException, PushWooshResponseException;
    List<Row> getResults(String requestId) throws IOException, PushWooshResponseException;
}
