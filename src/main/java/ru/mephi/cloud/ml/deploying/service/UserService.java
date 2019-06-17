package ru.mephi.cloud.ml.deploying.service;

import ru.mephi.cloud.ml.deploying.model.LoginResponseBody;
import ru.mephi.cloud.ml.deploying.model.UserLoginBody;

import java.io.IOException;
import java.util.Date;

/**
 * Created by kir on 06.04.19.
 */
public interface UserService {
    public String login(UserLoginBody userBody) throws IOException, InterruptedException;

    public LoginResponseBody updateAccessToken(Long userid);

    public Long isValidRefreshToken(String token);

    public boolean isValidAccessToken(String token, String username);

    public LoginResponseBody createTokensPair(String username, String password, String role);
}
