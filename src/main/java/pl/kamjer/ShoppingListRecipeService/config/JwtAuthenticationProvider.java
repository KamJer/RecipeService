package pl.kamjer.ShoppingListRecipeService.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserClient secClient;

    @Override
    public Authentication authenticate(Authentication authentication) {

        String token = (String) authentication.getCredentials();

        try {
            UserInfo isValid = secClient.isValid(token);
            log.debug("User '{}' authenticated successfully with role '{}'", isValid.getUserName(), isValid.getRole());
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            isValid.getUserName(),
                            null,
                            List.of(new SimpleGrantedAuthority(isValid.getRole()))
                    );

            auth.setDetails(token);

            return auth;
        } catch (HttpClientErrorException ex) {
            log.warn("Token validation failed: {}", ex.getMessage());
            throw new BadCredentialsException("Invalid token");
        } catch (Exception ex) {
            log.error("Token validation failed due to SecService error: {}", ex.getMessage());
            throw new BadCredentialsException("Authentication service unavailable");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthToken.class.isAssignableFrom(authentication);
    }
}
