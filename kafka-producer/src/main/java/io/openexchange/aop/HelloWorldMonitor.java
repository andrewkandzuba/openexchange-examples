package io.openexchange.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
public class HelloWorldMonitor {
    private final static String COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER = "counter.openexchange.kafka-producer";

    private final MetricRepository metricRepository;
    private final AtomicLong time;
    private final AtomicLong count;

    @Autowired
    public HelloWorldMonitor(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
        this.count = new AtomicLong(0);
        this.time = new AtomicLong(0);
    }

    @Pointcut("execution(* io.openexchange.controlllers.HelloWorldController.allocate())")
    public void allocateMethods() {
    }

    @Around("allocateMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object output = pjp.proceed();

        long requestTime = System.currentTimeMillis() - start;
        long totalRequestTime = time.addAndGet(requestTime);
        long totalCount = count.incrementAndGet();

        metricRepository.set(new Metric<Number>(COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER + ".allocate.avgRequestTime", (float) totalRequestTime / (float) totalCount));
        metricRepository.set(new Metric<Number>(COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER + ".allocate.totalRequestTime", totalRequestTime));
        metricRepository.set(new Metric<Number>(COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER + ".allocate.totalRequests", totalCount));

        return output;
    }
}
