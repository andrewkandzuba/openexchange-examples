package io.openexchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class HelloWorldController {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldController.class);
    @Autowired
    private Environment environment;
    private final static Random randomize = new Random();

    @RequestMapping("/hello")
    public String hello() {
        logger.debug("Hello World!!!");
        return "Hello World!!!";
    }

    @RequestMapping("/")
    public String query(@RequestParam("q") String q) {
        return environment.getProperty(q);
    }

    @RequestMapping("/next")
    public String next() {
        return Long.toString(randomize.nextLong());
    }

    @RequestMapping("/allocate")
    public String allocate() {
        byte[] bytes = new byte[1024];
        randomize.nextBytes(bytes);
        return new String(bytes);
    }
}
