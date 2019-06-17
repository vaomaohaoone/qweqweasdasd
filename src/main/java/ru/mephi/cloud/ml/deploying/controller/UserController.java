package ru.mephi.cloud.ml.deploying.controller;


import java.io.IOException;
import java.util.NoSuchElementException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mephi.cloud.ml.deploying.model.LoginResponseBody;
import ru.mephi.cloud.ml.deploying.model.UserLoginBody;
import ru.mephi.cloud.ml.deploying.service.UserService;

@RestController
@RequestMapping("/user")
@Api(value = "User controller",  description = "Operations with issuing and updating bearer tokens")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "Logging of user in system", response = ResponseEntity.class)
    @PostMapping(value = "login")
    public ResponseEntity login(@RequestBody final UserLoginBody login)
            throws ServletException, IOException, InterruptedException {
        String role = userService.login(login);
        if (login.name == null || role == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("No such user");
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.createTokensPair(login.name, login.password, role));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something wrong");
        }

    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer access_token",
                    required = true, dataType = "string", paramType = "header") })
    @ApiOperation(value = "Get expiration time of access token", response = ResponseEntity.class)
    @SuppressWarnings("unchecked")
    @GetMapping(value = "exp_time")
    public ResponseEntity getExpiration(final HttpServletRequest request) throws ServletException {
        final Claims claims = (Claims) request.getAttribute("claims");
        final String authHeader = request.getHeader("Authorization");
        final String token = authHeader.substring(7); // The part after "Bearer "
        if (userService.isValidAccessToken(token, claims.getSubject()))
            return ResponseEntity.status(HttpStatus.OK).body(claims.getExpiration().toString());
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Access token is not valid");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer access_token",
                    required = true, dataType = "string", paramType = "header") })
    @ApiOperation(value = "Updating of token pair", response = ResponseEntity.class)
    @GetMapping(value = "refresh_token")
    public ResponseEntity refreshToken(final HttpServletRequest request)
            throws ServletException, IOException, InterruptedException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServletException("Missing or invalid Authorization header.");
        }
        final String token = authHeader.substring(7); // The part after "Bearer "
        Long userid = userService.isValidRefreshToken(token);
        //Если токен валидный создаем и перезаписываем refresh_token в БД и access_token
        if (userid != 0) {
            return ResponseEntity.status(HttpStatus.OK).body(userService.updateAccessToken(userid));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new LoginResponseBody("", "", ""));
    }

}
