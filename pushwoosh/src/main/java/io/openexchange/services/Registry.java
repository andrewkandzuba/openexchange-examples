package io.openexchange.services;

import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.pushwoosh.PushWooshResponseException;

import java.io.IOException;

public interface Registry {
    boolean assign(User user, Application application, Device device) throws IOException, PushWooshResponseException;

    boolean add(Application application, Device device) throws IOException, PushWooshResponseException;

    boolean remove(Application application, Device device) throws IOException, PushWooshResponseException;
}