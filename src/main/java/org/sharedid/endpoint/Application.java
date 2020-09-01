package org.sharedid.endpoint;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(Application.class);
            app.setBannerMode(Banner.Mode.OFF);
            app.run(args);
        } catch (Exception e) {
            logger.error("Error thrown during Application startup", e);
            System.exit(1);
        }
    }
}
