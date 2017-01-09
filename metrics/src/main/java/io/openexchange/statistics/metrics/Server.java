package io.openexchange.statistics.metrics;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private final InetSocketAddress hostAndPort;
    private final Lock lock = new ReentrantLock();

    private long total;
    private long failures;
    private BigDecimal totalTime = BigDecimal.valueOf(0.0);

    public Server(InetSocketAddress hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public void update(Request rs) {
        lock.lock();
        try {
            total += 1;
            failures += rs.isSuccess() ? 0 : 1;
            totalTime = totalTime.add(BigDecimal.valueOf(rs.getTime()), MathContext.UNLIMITED);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "[" + "host:port=" + hostAndPort.getHostName() + ":" + hostAndPort.getPort() + ", " +
                "total=" + total + ", " +
                "failures=" + failures + ", " +
                "avgTime=" + ((total > 0) ? totalTime.divide(BigDecimal.valueOf(total), BigDecimal.ROUND_HALF_UP) : 0.0) + " mls." +
                "]";
    }
}
