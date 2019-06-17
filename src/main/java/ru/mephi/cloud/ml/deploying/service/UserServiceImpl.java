package ru.mephi.cloud.ml.deploying.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.cloud.ml.deploying.config.ConfigSwarm;
import ru.mephi.cloud.ml.deploying.model.LoginResponseBody;
import ru.mephi.cloud.ml.deploying.model.Token;
import ru.mephi.cloud.ml.deploying.model.Client;
import ru.mephi.cloud.ml.deploying.model.UserLoginBody;
import ru.mephi.cloud.ml.deploying.repository.TokenRepo;
import ru.mephi.cloud.ml.deploying.repository.ClientRepo;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by kir on 06.04.19.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private TokenRepo tokenRepo;

    @Autowired
    private ConfigSwarm configSwarm;

    public String login(UserLoginBody userBody) throws IOException, InterruptedException {
        Map<UserLoginBody, String> logmap;
        String usr_pswd = RegistrationServiceImpl.generateHash(userBody.password);
        List<Client> clients = clientRepo.findByUsernameAndPassword(userBody.name, usr_pswd);
        if (!clients.isEmpty())
            return clients.get(0).getRole();
        else
            return null;
    }

    public LoginResponseBody createTokensPair(String username, String password, String role) {
        //access_token на 30 минут зашифровано username
        Date exp_date1 = new Date(System.currentTimeMillis() + configSwarm.getTtlAccessToken());
        //refresh_token на 60 дней зашифровано userid
        Date exp_date2 = new Date(System.currentTimeMillis() + configSwarm.getTtlRefreshToken());
        //генерация access токена
        String access_token = createAccessToken(username, role, exp_date1);
        //извлечение пользователя с таким именем и паролем
        List<Client> clients = clientRepo.findByUsernameAndPassword(username, RegistrationServiceImpl.generateHash(password));
        if (!clients.isEmpty()) {
            //генерация refresh токена
            String refresh_token = createRefreshToken(role, clients.get(0).getId(), exp_date2);
            //запись токена в БД
            writeToken(clients.get(0), refresh_token, access_token);
            return new LoginResponseBody(refresh_token, access_token, exp_date1.toString());
        } else return new LoginResponseBody("null", "null", "null");
    }

    public boolean isValidAccessToken(String token, String username) {
        boolean valid = false;
        List<Client> clients = clientRepo.findByUsername(username);
        if (!clients.isEmpty()) {
            List<Token> token_from_db = tokenRepo.findByClient(clients.get(0));
            if (!token_from_db.isEmpty()) {
                String acc_tkn = token_from_db.get(0).getAccess_token();
                if (acc_tkn.equals(token))
                    valid = true;
            }
        }
        return valid;
    }

    public Long isValidRefreshToken(String token) {
        Long valid = 0L;
        final Claims claims = Jwts.parser().setSigningKey("TimoFeevKey1")
                .parseClaimsJws(token).getBody();
        if (claims.get("token_type").toString().equals("refresh token")) {
            String userid = claims.getSubject();
            Client client = clientRepo.findById(Long.valueOf(userid)).get();
            List<Token> res = tokenRepo.findByClient(client);
            if (!res.isEmpty()) {
                if (token.equals(res.get(0).getRefresh_token()))
                    valid = res.get(0).getClient().getId();
            }

        }
        return valid;
    }

    public LoginResponseBody updateAccessToken(Long userid) {
        Optional<Client> clients = clientRepo.findById(userid);
        if (clients.isPresent()) {
            String role = clients.get().getRole();
            //access_token на 30 минут зашифровано username
            Date exp_date1 = new Date(System.currentTimeMillis() + configSwarm.getTtlAccessToken());
            String access_token = createAccessToken(role, clients.get().getUsername(), exp_date1);
            //refresh_token на 60 дней зашифровано userid
            Date exp_date2 = new Date(System.currentTimeMillis() + configSwarm.getTtlRefreshToken());
            String refresh_token = createRefreshToken(role, userid, exp_date2);
            writeToken(clients.get(), refresh_token, access_token);
            return new LoginResponseBody(refresh_token, access_token, exp_date1.toString());
        } else
            return new LoginResponseBody("null", "null", "null");
    }

    private String createRefreshToken(String role, Long userid, Date exp_date) {
        return Jwts.builder().setSubject(userid.toString())
                .claim("roles", role).setIssuedAt(new Date())
                .claim("token_type", "refresh token")
                .setExpiration(exp_date)
                .signWith(SignatureAlgorithm.HS256, "TimoFeevKey1").compact();
    }

    private String createAccessToken(String username, String role, Date exp_date) {
        return Jwts.builder().setSubject(username)
                .claim("roles", role).setIssuedAt(new Date())
                .claim("token_type", "access_token")
                .setExpiration(exp_date)
                .signWith(SignatureAlgorithm.HS256, "TimoFeevKey1").compact();
    }

    @Transactional(timeout = 10)
    private void writeToken(Client client, String refresh_token, String access_token) {
        Token token = new Token(refresh_token, access_token);
        List<Token> token_from_db = tokenRepo.findByClient(client);
        token.setClient(client);
        if (!token_from_db.isEmpty()) {
            token.setId(token_from_db.get(0).getId());
        }
        tokenRepo.save(token);
    }
}
