package vlinh.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import vlinh.User;

import java.util.Date;

public class JwtGenerator {

    public String generate(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() +  24 * 3660 * 1000); // 1 day

        Algorithm algorithm = Algorithm.HMAC256(JwtSecret.SECRET_KEY);

        String token = JWT.create()
                .withSubject(user.getUsername())
                .withClaim("id", user.getId())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(algorithm);

        return token;
    }
}
