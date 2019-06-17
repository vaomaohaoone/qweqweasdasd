package ru.mephi.cloud.ml.deploying.controller;

import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mephi.cloud.ml.deploying.model.DeployBody;
import ru.mephi.cloud.ml.deploying.model.ImageBody;
import ru.mephi.cloud.ml.deploying.model.ImageContainer;
import ru.mephi.cloud.ml.deploying.service.ApiService;
import ru.mephi.cloud.ml.deploying.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by kir on 06.04.19.
 */

@RestController
@RequestMapping("/user/api")
@Api(value = "Swarm operations", description = "Push, pull, deploy and get models")
public class ApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private ApiService apiService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer access_token",
                    required = true, dataType = "string", paramType = "header") })
    @ApiOperation(value = "Pushing model to docker hub registry", response = ResponseEntity.class)
    @PostMapping(value = "push", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   // @ResponseBody
    public ResponseEntity push(final HttpServletRequest request, @ModelAttribute ImageBody imageBody) throws IOException, InterruptedException {
        final Claims claims = (Claims) request.getAttribute("claims");
        final String authHeader = request.getHeader("Authorization");
        final String token = authHeader.substring(7); // The part after "Bearer "
        if (userService.isValidAccessToken(token, claims.getSubject())) {
            String username = claims.getSubject();
            String role = claims.get("roles").toString();
            String port = imageBody.getPort();
            List<String> list = apiService.busyPorts();
            if (!list.contains(port)) {
                apiService.build(username, role, port, imageBody.getName(), imageBody.getZip());
                return ResponseEntity.status(HttpStatus.OK).body("OK");
            } else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid port: such port already used");
        } else
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Incorrect access token: you are fake!");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer access_token",
                    required = true, dataType = "string", paramType = "header") })
    @ApiOperation(value = "Pulling model from docker hub registry and deploying it on swarm cluster", response = ResponseEntity.class)
    @PostMapping(value = "pull")
    @ResponseBody
    public ResponseEntity pull(final HttpServletRequest request, @RequestBody DeployBody body) throws IOException, InterruptedException {
        final Claims claims = (Claims) request.getAttribute("claims");
        final String authHeader = request.getHeader("Authorization");
        final String token = authHeader.substring(7); // The part after "Bearer "
        if (userService.isValidAccessToken(token, claims.getSubject())) {
            String username = claims.getSubject();
            apiService.run(body.getName(), body.getTag(), body.getReplicas(), body.getRam(), body.getCpu(), username);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully pull on your port");
        }
        else
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Something wrong");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer access_token",
                    required = true, dataType = "string", paramType = "header") })
    @ApiOperation(value = "Get already busy ports by models", response = ResponseEntity.class)
    @GetMapping(value = "busyPorts")
    public ResponseEntity getBusyPorts(final HttpServletRequest request) throws IOException, InterruptedException {
        final Claims claims = (Claims) request.getAttribute("claims");
        final String authHeader = request.getHeader("Authorization");
        final String token = authHeader.substring(7); // The part after "Bearer "
        if (userService.isValidAccessToken(token, claims.getSubject())) {
            return ResponseEntity.status(HttpStatus.OK).body(apiService.busyPorts());
        } else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("BAD");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer access_token",
                    required = true, dataType = "string", paramType = "header") })
    @ApiOperation(value = "Get models, that was build", response = List.class)
    @GetMapping(value = "models")
    public Map<String, String> models(final HttpServletRequest request) {
        final Claims claims = (Claims) request.getAttribute("claims");
        final String authHeader = request.getHeader("Authorization");
        final String token = authHeader.substring(7); // The part after "Bearer "
        if (userService.isValidAccessToken(token, claims.getSubject())) {
            String username = claims.getSubject();
            return apiService.getModels(username);
        } else return new HashMap<>();
    }
}
