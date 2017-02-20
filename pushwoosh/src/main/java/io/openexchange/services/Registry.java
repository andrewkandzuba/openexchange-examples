package io.openexchange.services;

import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pushwoosh.PushWooshResponseException;

import java.io.IOException;

public interface Registry {
    Reply assign(User user, Application application, Device device) throws IOException, PushWooshResponseException;

    Reply add(Application application, Device device) throws IOException, PushWooshResponseException;

    Reply remove(Application application, Device device) throws IOException, PushWooshResponseException;
}