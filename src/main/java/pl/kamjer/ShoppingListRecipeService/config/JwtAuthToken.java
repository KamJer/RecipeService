package pl.kamjer.ShoppingListRecipeService.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class JwtAuthToken extends AbstractAuthenticationToken {

    private String token;

    public JwtAuthToken(String token) {
        super(List.of());
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
