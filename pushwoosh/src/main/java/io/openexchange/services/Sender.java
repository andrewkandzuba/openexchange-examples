package io.openexchange.services;

import io.openexchange.domain.Application;
import io.openexchange.domain.Device;
import io.openexchange.domain.User;

import java.io.IOException;

public interface Sender {
    boolean push(Application application, String text, User ...users) throws IOException;
    boolean push(Application application, String text, Device ...devices) throws IOException;
}
