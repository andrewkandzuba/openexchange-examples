package io.openexchange.services;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.User;
import io.openexchange.pushwoosh.PushWooshResponseException;

import java.io.IOException;
import java.util.List;

public interface Sender {
    List<String> push(Application application, String text, User ...users) throws IOException, PushWooshResponseException;
    List<String> push(Application application, String text, Device ...devices) throws IOException, PushWooshResponseException;
}
