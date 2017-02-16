package io.openexchange.services;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.User;

import java.io.IOException;

public interface Registry {
    boolean assign(User user, Application application, Device device) throws IOException;
    boolean add(Application application, Device device) throws IOException;
    boolean remove(Application application, Device device) throws IOException;
}