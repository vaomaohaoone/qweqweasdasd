package ru.mephi.cloud.ml.deploying.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.cloud.ml.deploying.model.ImageContainer;

import java.util.List;

/**
 * Created by kir on 06.04.19.
 */
public interface ImageContainerRepo extends JpaRepository<ImageContainer, Long> {
    List<ImageContainer> findByNameAndTag(String name, String tag);
    List<ImageContainer> findByName(String name);
    List<ImageContainer> findByClient(Long userid);
}
