package ru.mephi.cloud.ml.deploying.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.cloud.ml.deploying.model.Model;
import ru.mephi.cloud.ml.deploying.model.Version;

import java.util.List;

/**
 * Created by kir on 07.05.19.
 */
public interface VersionRepo extends JpaRepository<Version, Long> {

    List<Version> findByModel(Model model);
}
