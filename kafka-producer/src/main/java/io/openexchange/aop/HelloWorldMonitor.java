package io.openexchange.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HelloWorldMonitor {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldMonitor.class);

    @Pointcut("execution(* io.openexchange.controlllers.HelloWorldController.allocate())")
    public void allocateMethods() { }

    @Around("allocateMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object output = pjp.proceed();
        logger.info("Method execution time: " + (System.currentTimeMillis() - start) + " milliseconds.");
        return output;
    }
}
