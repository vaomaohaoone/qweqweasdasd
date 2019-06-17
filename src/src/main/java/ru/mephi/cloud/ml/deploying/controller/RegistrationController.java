package ru.mephi.cloud.ml.deploying.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.mephi.cloud.ml.deploying.model.RegistrationBody;
import ru.mephi.cloud.ml.deploying.service.RegistrationService;


import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Created by kir on 27.02.19.
 */

@Controller
@Api(value = "User registration", description = "Registration of user in system")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @ApiOperation(value = "Registration", response = ResponseEntity.class)

    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity registration(@RequestBody RegistrationBody body) throws IOException {
        try {
            boolean reg = registrationService.registration(body);
            if(reg) {
                return ResponseEntity.status(HttpStatus.OK).body("Registration completed successfully!");
            }
            else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Wrong! Such client already exist!");
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something wrong");
        }
    }
}
