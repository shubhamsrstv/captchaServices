package com.proxy.services.captchaservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@CrossOrigin
public class CaptchaController {

    @Value("${olb.captcha.url}")
    private String url;

    @Value("${olb.captcha.secretkey}")
    private String secret;

    @PostMapping(value = "/captcha/verification", produces = "application/json")
    @ResponseBody
    public int captchaVerification(@RequestParam String response) throws IOException {
        String param = "secret="+secret+"&response="+response;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(param.getBytes());
        os.flush();
        os.close();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder captchaResponse = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                captchaResponse.append(inputLine);
            }
            in.close();
            if (new ObjectMapper().readValue(captchaResponse.toString(), JSONObject.class).getString("success").equalsIgnoreCase("true")) {
                return 0;
            }
            else {
                return 1;
            }
        }
        else {
            return 1;
        }
    }
}