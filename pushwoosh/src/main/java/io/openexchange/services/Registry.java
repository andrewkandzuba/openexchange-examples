package io.openexchange.services;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.User;

public interface Registry {
    boolean assign(User user, Application application, Device device);
}
