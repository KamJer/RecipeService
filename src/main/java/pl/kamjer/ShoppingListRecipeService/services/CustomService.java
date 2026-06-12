package pl.kamjer.ShoppingListRecipeService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;
import pl.kamjer.ShoppingListRecipeService.model.User;
import pl.kamjer.ShoppingListRecipeService.model.dto.UserDto;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CustomService {

    protected UserClient secClient;
    protected ObjectMapper objectMapper;

    public User getUserFromAuth() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> {
                    try {
                        String userName = auth.getPrincipal().toString();
                        String token = Optional.ofNullable(auth.getDetails())
                                .map(Object::toString)
                                .orElse(null);
                        if (token == null) {
                            log.warn("No token found in authentication details for user: {}", userName);
                            return null;
                        }
                        UserDto userDto = secClient.getUserByUserName(userName, token);
                        return objectMapper.convertValue(userDto, User.class);
                    } catch (Exception e) {
                        log.error("Failed to fetch user from auth service: {}", e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }
}
