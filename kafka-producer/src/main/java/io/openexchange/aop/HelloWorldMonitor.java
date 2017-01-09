package io.openexchange.aop;

import io.openexchange.statistics.Tracking;
import io.openexchange.statistics.metrics.Request;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Aspect
@Component
public class HelloWorldMonitor {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldMonitor.class);
    private final Tracking statistics;
    private final Environment environment;

    @Autowired
    public HelloWorldMonitor(Tracking statistics, Environment environment) {
        this.statistics = statistics;
        this.environment = environment;
    }

    @Pointcut("execution(* io.openexchange.controlllers.HelloWorldController.allocate())")
    public void allocateMethods() {
    }

    @Around("allocateMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object output = pjp.proceed();
        statistics.log(InetSocketAddress.createUnresolved("localhost", Integer.valueOf(environment.getProperty("server.port"))),
                new Request(true, System.currentTimeMillis() - start));
        return output;
    }
}
