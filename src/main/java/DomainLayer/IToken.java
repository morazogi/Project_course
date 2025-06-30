package DomainLayer;

import io.jsonwebtoken.Claims;

import java.util.*;
import java.util.function.Function;

public interface IToken {
    public String generateToken(String username);
    public void validateToken(String token);
    public String extractUsername(String token);
    public Date extractExpiration(String token);
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    public Claims extractAllClaims(String token);
    public void invalidateToken(String token);
    public void suspendUser(String token);
    public void unsuspendUser(String token);
    public List<String> showSuspended();
    public String getToken(String username);
}