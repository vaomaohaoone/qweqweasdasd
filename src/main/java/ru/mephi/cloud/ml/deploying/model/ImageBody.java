package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by kir on 06.04.19.
 */
@Data
@NoArgsConstructor(force = true)
public class ImageBody {

    private MultipartFile zip;
    private String name;
    private String port;
   // private String tag;
}
