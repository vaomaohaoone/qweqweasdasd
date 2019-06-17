package ru.mephi.cloud.ml.deploying.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.mephi.cloud.ml.deploying.model.Client;
import ru.mephi.cloud.ml.deploying.model.Token;

import java.util.List;

/**
 * Created by kir on 06.04.19.
 */
public interface TokenRepo extends JpaRepository<Token, Long>
{
    List<Token> findByClient(Client client);
}
