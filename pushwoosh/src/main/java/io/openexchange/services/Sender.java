package io.openexchange.services;

import io.openexchange.domain.Application;
import io.openexchange.domain.User;

public interface Sender {
    boolean push(Application app, String text, User ...users);
}
