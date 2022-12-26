package vlinh;

import lombok.AllArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vlinh.api.JwtValidationRequest;
import vlinh.api.UserLoginRequest;
import vlinh.api.UserRegistrationRequest;
import vlinh.jwt.JwtAuthenticationException;
import vlinh.jwt.JwtGenerator;
import vlinh.jwt.JwtValidator;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    public ResponseEntity<Object> registerUser(UserRegistrationRequest request) throws JSONException {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.set("Content-Type", "application/json");

        // check null value
        if (user.getUsername() == null || user.getPassword() == null) {
            JSONObject responseBody = new JSONObject()
                    .put("message","Missing credentials")
                    .put("success", false);
            return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.BAD_REQUEST);
        }

        // check if username already exist
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if (optionalUser.isPresent()) {
            JSONObject responseBody = new JSONObject()
                    .put("message","Username already exist")
                    .put("success", false);
            return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.CONFLICT);
        }

        Integer userId = userRepository.save(user).getId();
        JSONObject responseBody = new JSONObject()
                .put("user_id", userId)
                .put("success",true);
        return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.OK);
    }

    public ResponseEntity<Object> login(UserLoginRequest request) throws JSONException {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.set("Content-Type", "application/json");

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if (optionalUser.isEmpty()) {
            JSONObject responseBody = new JSONObject()
                    .put("message","Username not exist")
                    .put("success", false);
            return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.UNAUTHORIZED);
        }
        if (!user.getPassword().equals(optionalUser.get().getPassword())) {
            JSONObject responseBody = new JSONObject()
                    .put("message", "Wrong password")
                    .put("success", false);
            return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.UNAUTHORIZED);
        }

        String token = new JwtGenerator().generate(user);
        JSONObject responseBody = new JSONObject()
                .put("token", token)
                .put("success",true);
        return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.OK);
    }

    public ResponseEntity<Object> validate(JwtValidationRequest request) throws JSONException {
        HttpHeaders responseHeader = new HttpHeaders();
        responseHeader.set("Content-Type", "application/json");
        try {
            User user = new JwtValidator().validate(request.getToken());
            System.out.println("User: " + user);
            JSONObject responseBody = new JSONObject()
                    .put("success",true);
            return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.OK);
        } catch (JwtAuthenticationException e) {
            JSONObject responseBody = new JSONObject()
                    .put("message", "Not authorized")
                    .put("success",false);
            return new ResponseEntity<>(responseBody.toString(), responseHeader, HttpStatus.FORBIDDEN);
        }
    }
}
