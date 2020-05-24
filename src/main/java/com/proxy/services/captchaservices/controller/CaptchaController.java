package com.proxy.services.captchaservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proxy.services.captchaservices.beans.RecaptchaUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class CaptchaController {

    @Value("${olb.captcha.url}")
    private String url;

    @Value("${olb.captcha.secretkey}")
    private String secret;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

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

    @PostMapping(value = "/captcha/verification/v2", produces = "application/json")
    @ResponseBody
    public String captchaVerificationV2(@RequestParam String response) throws JsonProcessingException {
        Map<String, String> body = new HashMap<>();
        body.put("secret", secret);
        body.put("response", response);
        System.out.println("Request body for recaptcha: {}"+ body);
        ResponseEntity<Map> recaptchaResponseEntity =
                restTemplateBuilder.build()
                        .postForEntity(url+
                                        "?secret={secret}&response={response}",
                                body, Map.class, body);
        System.out.println("Response from recaptcha: {}"+ recaptchaResponseEntity);
        Map<String, Object> responseBody = recaptchaResponseEntity.getBody();
        boolean recaptchaSucess = (Boolean)responseBody.get("success");
        System.out.println(recaptchaSucess);
        Map<String, String> result = new HashMap<>();
        if ( !recaptchaSucess) {
            List<String> errorCodes = (List)responseBody.get("error-codes");
            result.put("result", errorCodes.stream()
                    .map(s -> RecaptchaUtil.RECAPTCHA_ERROR_CODE.get(s))
                    .collect(Collectors.joining(", ")));
        }else {
            result.put("response","Captcha matched successfully");
        }
        return new ObjectMapper().writeValueAsString(result);
    }
}