package ru.mephi.cloud.ml.deploying.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by kir on 26.04.19.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "swarm")
public class ConfigSwarm {
    String skladDirectory;
    String zipDirectory;
    String dockerFile;
    String appFile;
    String dockerHubRepository;
    String registryEmail;
    String registryPassword;
    String registryUsername;
    String managerNode;
    String fingerprint;
    String usernameSwarm;
    String passwordSwarm;
    Long ttlAccessToken;
    Long ttlRefreshToken;
}
