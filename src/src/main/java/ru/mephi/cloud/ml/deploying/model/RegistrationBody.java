package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by kir on 06.04.19.
 */
@Data
@NoArgsConstructor(force = true)
public class RegistrationBody {
    private String user;
    private String password;
    private String role;
}
