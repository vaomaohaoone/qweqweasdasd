package ru.mephi.cloud.ml.deploying.model;

import lombok.Data;

/**
 * Created by kir on 27.02.19.
 */
@Data
public class LoginResponseBody {
    public String refresh_token;
    public String access_token;
    public String exp_date;

    public LoginResponseBody(final String refresh_token, final String access_token, final String exp_date) {
        this.refresh_token = refresh_token;
        this.access_token = access_token;
        this.exp_date = "EXPIRATION DATE:  " + exp_date;
    }
}
