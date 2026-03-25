package pl.kamjer.ShoppingListRecipesServics.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipesServics.client.UserClient;
import pl.kamjer.ShoppingListRecipesServics.model.User;

@Service
@AllArgsConstructor
public class CustomService {

    protected UserClient secClient;
    protected ObjectMapper objectMapper;

    public User getUserFromAuth() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String token = SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
        return objectMapper.convertValue(secClient.getUserByUserName(userName, token), User.class);
    }
}
