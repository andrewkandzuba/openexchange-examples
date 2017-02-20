package io.openexchange.services;

import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pushwoosh.PushWooshResponseException;

import java.io.IOException;

public interface Sender {
    PushReply push(Application application, String text, User user) throws IOException, PushWooshResponseException;

    PushReply push(Application application, String text, Device device) throws IOException, PushWooshResponseException;
}
