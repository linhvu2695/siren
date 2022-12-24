package vlinh.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import vlinh.User;

public class JwtValidator {
    public User validate(String token) throws JwtAuthenticationException {
        User user;
        try {
            Algorithm algorithm = Algorithm.HMAC256(JwtSecret.SECRET_KEY);
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);

            user = new User();
            user.setUsername(decodedJWT.getSubject());
            user.setId(decodedJWT.getClaim("id").asInt());
        } catch (JWTDecodeException e) {
            throw new JwtAuthenticationException("Expired of invalid JWT token");
        }
        return user;
    }
}
