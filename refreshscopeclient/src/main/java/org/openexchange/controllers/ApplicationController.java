package org.openexchange.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ApplicationController {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    public ApplicationController() {
        logger.info("Re-initialize");
    }

    @Value("${org.openexchange.examples.refreshscope.language:EN}")
    private String language;

    @RequestMapping("/language")
    public String language() {
        return language;
    }
}
