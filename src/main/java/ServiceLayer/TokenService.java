package ServiceLayer;

import DomainLayer.IToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class TokenService implements IToken {

    private final String secret = "fzhfbvklyanivlkd675548!oiu8dhf=="; // make sure it's 256 bits (32 chars)
    private final SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

    private final long expirationTime = 1000 * 60 * 60; // one hour

    private final Set<String> blacklistedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();
    private final Set<String> suspendedUsers = new HashSet<>();

    public String generateToken(String username) {
        EventLogger.logEvent(username ,"TokenService");
        String JWT = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
        activeTokens.put(JWT, username);
        return JWT;
    }

    public void validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (blacklistedTokens.contains(token)) {
            throw new IllegalArgumentException("user not logged in");
        }

        if (!activeTokens.containsKey(token))
            throw new IllegalArgumentException("Token is not active");
        if (suspendedUsers.contains(extractUsername(token))) {
            throw new IllegalArgumentException("User is suspended");
        }

        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String extractUsername(String token) {
       return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (blacklistedTokens.contains(token)) {
            throw new IllegalArgumentException("user not logged in");
        }
        blacklistedTokens.add(token);
        activeTokens.remove(extractUsername(token) , token);
    }

    private static void requireNonEmpty(String token) {
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Token cannot be null or empty");
    }


    public void suspendUser(String username){
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (suspendedUsers.contains(username)) {
            throw new IllegalArgumentException("User is already suspended");
        }
        suspendedUsers.add(username);
        EventLogger.logEvent(username , "User suspended");
    }

    public void unsuspendUser(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (!suspendedUsers.contains(username)) {
            throw new IllegalArgumentException("User is not suspended");
        }
        suspendedUsers.remove(username);
        EventLogger.logEvent(username , "User unsuspended");
    }

    public List<String> showSuspended() {
        List<String> suspendedList = new ArrayList<>();
        for (String username : suspendedUsers) {
            suspendedList.add(username);
        }
        return suspendedList;
    }

    public String getToken(String username) {
        for(String token : activeTokens.keySet()) {
            if (activeTokens.get(token).equals(username)) {
                return token;
            }
        }
        return "";
    }
}