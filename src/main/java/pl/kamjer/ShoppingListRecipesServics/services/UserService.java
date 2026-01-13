package pl.kamjer.ShoppingListRecipesServics.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import pl.kamjer.ShoppingListRecipesServics.client.UserClient;
import pl.kamjer.ShoppingListRecipesServics.model.User;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private UserClient userClient;
    private ObjectMapper objectMapper;

    public Optional<User> getUserFromAuth() {
        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            return Optional.of(objectMapper.convertValue(userClient.getUserByUserName(userName), User.class));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }

    }
}
