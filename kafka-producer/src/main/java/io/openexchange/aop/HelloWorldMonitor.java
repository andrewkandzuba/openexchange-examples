package io.openexchange.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HelloWorldMonitor {
    private final static String COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER = "counter.openexchange.kafka-producer";

    private final MetricRepository metricRepository;

    @Autowired
    public HelloWorldMonitor(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Pointcut("execution(* io.openexchange.controlllers.HelloWorldController.allocate())")
    public void allocateMethods() {
    }

    @Around("allocateMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object output = pjp.proceed();
        metricRepository.increment(new Delta<>(COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER + ".time", System.currentTimeMillis() - start));
        metricRepository.increment(new Delta<Number>(COUNTER_OPENEXCHANGE_KAFKA_PRODUCER_SERVER + ".success", 1));
        return output;
    }
}
