package pl.kamjer.ShoppingListRecipesServics.client;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.kamjer.ShoppingListRecipesServics.model.dto.UserDto;
import pl.kamjer.ShoppingListRecipesServics.model.dto.UserRequestDto;

@Component
@AllArgsConstructor
public class UserClient {

    private final RestClient userRestClient;

    public UserDto getUserByUserName(String userName) {
        return userRestClient
                .get()
                .uri("/{userName}", userName)
                .retrieve()
                .body(UserDto.class);
    }

    public Boolean logUser(UserRequestDto userRequestDto) {
        return userRestClient
                .post()
                .uri("/log")
                .body(userRequestDto)
                .retrieve()
                .body(Boolean.class);
    }


}
