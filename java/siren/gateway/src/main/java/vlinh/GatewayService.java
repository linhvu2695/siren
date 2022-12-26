package vlinh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import vlinh.api.UserLoginRequest;
import vlinh.api.UserRegistrationRequest;
import vlinh.model.Request;

@Service
@AllArgsConstructor
public class GatewayService {
    private final RequestRepository requestRepository;
    private final RestTemplate restTemplate;

    public ResponseEntity<Object> register(UserRegistrationRequest userRegistrationRequest) throws JsonProcessingException {
        saveRequest(HttpMethod.POST, userRegistrationRequest);
        try {
            return restTemplate.postForEntity(
                    "http://localhost:8081/api/v1/user/register",
                    userRegistrationRequest,
                    Object.class
            );
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    public ResponseEntity<Object> login(UserLoginRequest userLoginRequest) throws JsonProcessingException {
        saveRequest(HttpMethod.POST, userLoginRequest);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    "authorization",
                    "Basic " + userLoginRequest.getUsername() + ":" + userLoginRequest.getPassword());

            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    "http://localhost:8081/api/v1/user/login",
                    HttpMethod.POST,
                    requestEntity,
                    Object.class);
            return response;
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    private void saveRequest(HttpMethod method, Object requestObject) throws JsonProcessingException {
        Request request = Request.builder()
                .method(method)
                .content(new ObjectMapper().writeValueAsString(requestObject))
                .type(requestObject.getClass().getSimpleName())
                .build();
        requestRepository.save(request);
    }
}
