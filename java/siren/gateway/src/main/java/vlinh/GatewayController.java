package vlinh;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vlinh.api.UserLoginRequest;
import vlinh.api.UserRegistrationRequest;

import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/")
public class GatewayController {
    private final GatewayService gatewayService;

    @PostMapping(path = "/register",
            consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Object> register(@RequestBody UserRegistrationRequest userRegistrationRequest) throws JsonProcessingException {
        log.info("New user registration {}", userRegistrationRequest);
        return gatewayService.register(userRegistrationRequest);
    }

    @PostMapping(path = "/login",
            consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Object> login(@RequestHeader Map<String, String> authorizationHeader) throws JSONException, JsonProcessingException {
        log.info("User login authorization {}", authorizationHeader);
        if (!authorizationHeader.containsKey("authorization")) {
            return missingCredentialsResponse();
        }
        String[] credentials = authorizationHeader.get("authorization").split(" ")[1].split(":");
        UserLoginRequest userLoginRequest = new UserLoginRequest(credentials[0], credentials[1]);
        return gatewayService.login(userLoginRequest);
    }

    private ResponseEntity<Object> missingCredentialsResponse() throws JSONException {
        JSONObject responseBody = new JSONObject()
                .put("message","Missing credentials")
                .put("success", false);
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.set("Content-Type", "application/json");
        return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.UNAUTHORIZED);
    }
}
