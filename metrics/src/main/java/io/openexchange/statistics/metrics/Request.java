package io.openexchange.statistics.metrics;

public class Request {
    private final boolean success;
    private final long time;

    public Request(boolean success, long time) {
        this.success = success;
        this.time = time;
    }

    boolean isSuccess() {
        return success;
    }

    long getTime() {
        return time;
    }
}
