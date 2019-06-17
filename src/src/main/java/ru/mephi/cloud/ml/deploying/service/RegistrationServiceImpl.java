package ru.mephi.cloud.ml.deploying.service;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.cloud.ml.deploying.model.RegistrationBody;
import ru.mephi.cloud.ml.deploying.model.Client;
import ru.mephi.cloud.ml.deploying.repository.ClientRepo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * Created by kir on 27.02.19.
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Autowired
    private ClientRepo repository;

    public static final String SALT = "my-salt-text";

    @Transactional(timeout = 10)
    public boolean registration(RegistrationBody body) {
        List<Client> clientList = repository.findByUsername(body.getUser());
        boolean result = false;
        if(clientList.isEmpty()) {
            repository.save(new Client(body.getUser(), generateHash(body.getPassword()), body.getRole()));
            result = true;
        }
        return result;
    }


    // сделан public, для доступа классом UserServiceImpl
    public static String generateHash(String input) {
        StringBuilder builder = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            builder.append(new String(Hex.encode(hash)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}
