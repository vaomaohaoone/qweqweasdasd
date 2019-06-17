package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by kir on 27.02.19.
 */
@Data
@NoArgsConstructor(force = true)
public class UserLoginBody {
    public String name;
    public String password;
}
