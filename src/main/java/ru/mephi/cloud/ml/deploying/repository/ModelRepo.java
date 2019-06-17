package ru.mephi.cloud.ml.deploying.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.cloud.ml.deploying.model.Client;
import ru.mephi.cloud.ml.deploying.model.Model;

import java.util.List;

/**
 * Created by kir on 06.04.19.
 */
public interface ModelRepo extends JpaRepository<Model, Integer> {
    List<Model> findByName(String name);
    List<Model> findByClients(Client clients);
}
