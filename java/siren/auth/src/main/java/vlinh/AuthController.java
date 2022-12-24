package vlinh;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vlinh.api.JwtValidationRequest;
import vlinh.api.UserLoginRequest;
import vlinh.api.UserRegistrationRequest;

import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/user")
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/register",
            consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<Object> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) throws JSONException {
        log.info("New user registration {}", userRegistrationRequest);
        return authService.registerUser(userRegistrationRequest);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<Object> login(@RequestHeader Map<String, String> authorizationHeader) throws JSONException {
        log.info("User login authorization {}", authorizationHeader);
        if (!authorizationHeader.containsKey("authorization")) {
            return missingCredentialsResponse();
        }
        String[] credentials = authorizationHeader.get("authorization").split(" ")[1].split(":");
        UserLoginRequest userLoginRequest = new UserLoginRequest(credentials[0], credentials[1]);
        return authService.login(userLoginRequest);
    }

    @PostMapping(path = "/validate")
    public ResponseEntity<Object> validate(@RequestHeader Map<String, String> authorizationHeader) throws JSONException {
        log.info("Validation authorization header: {}", authorizationHeader);
        if (!authorizationHeader.containsKey("authorization")) {
            return missingCredentialsResponse();
        }
        String token = authorizationHeader.get("authorization").split(" ")[1];
        JwtValidationRequest jwtValidationRequest = new JwtValidationRequest(token);
        return authService.validate(jwtValidationRequest);
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
