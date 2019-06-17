package ru.mephi.cloud.ml.deploying.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.cloud.ml.deploying.model.Client;

import java.util.List;

/**
 * Created by kir on 06.04.19.
 */
public interface ClientRepo extends JpaRepository<Client, Long> {
    List<Client> findByUsernameAndPassword(String username, String password);

    List<Client> findByUsername(String username);

}
