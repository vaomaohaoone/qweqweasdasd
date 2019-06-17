package ru.mephi.cloud.ml.deploying.service;

import org.springframework.web.multipart.MultipartFile;
import ru.mephi.cloud.ml.deploying.model.ImageContainer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by kir on 06.04.19.
 */
public interface ApiService {
    public List<String> busyPorts() throws IOException;

    public void build(String username, String role, String port, String name, MultipartFile zip) throws InterruptedException, IOException;

    public void run(String name, String tag, String replicas, String ram, String cpu, String username) throws InterruptedException, IOException;

    public Map<String,String> getModels(String username);
}
