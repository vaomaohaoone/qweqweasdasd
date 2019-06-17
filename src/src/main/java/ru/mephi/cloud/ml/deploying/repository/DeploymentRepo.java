package ru.mephi.cloud.ml.deploying.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.cloud.ml.deploying.model.Deployment;

/**
 * Created by kir on 06.04.19.
 */
public interface DeploymentRepo extends JpaRepository<Deployment, Integer> {

}
