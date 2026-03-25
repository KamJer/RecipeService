package pl.kamjer.ShoppingListRecipesServics.config;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pl.kamjer.ShoppingListRecipesServics.client.UserClient;
import pl.kamjer.ShoppingListRecipesServics.model.dto.UserRequestDto;

import java.util.List;

@Component
@AllArgsConstructor
public class RemoteAuthProvider implements AuthenticationProvider {

    private UserClient userClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();

        if (!userClient.logUser(new UserRequestDto(username, null))) {
            throw new BadCredentialsException("Invalid credentials");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
