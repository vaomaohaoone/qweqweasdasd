package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by kir on 06.04.19.
 */
@Data
@NoArgsConstructor(force = true)
public class DeployBody {
    private String name;
    private String tag;
    private String replicas;
    private String cpu;
    private String ram;
}
