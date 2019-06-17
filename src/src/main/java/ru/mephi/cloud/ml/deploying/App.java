package ru.mephi.cloud.ml.deploying;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.mephi.cloud.ml.deploying.config.ConfigSwarm;

/**
 * Created by kir on 18.10.18.
 */

@SpringBootApplication
@EnableConfigurationProperties(ConfigSwarm.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

