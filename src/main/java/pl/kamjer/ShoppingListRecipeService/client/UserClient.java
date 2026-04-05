package pl.kamjer.ShoppingListRecipeService.client;

import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.kamjer.ShoppingListRecipeService.config.UserInfo;
import pl.kamjer.ShoppingListRecipeService.model.dto.UserDto;
import pl.kamjer.ShoppingListRecipeService.model.dto.UserRequestDto;

@Component
@AllArgsConstructor
public class UserClient {

    private final RestClient userRestClient;

    public UserDto getUserByUserName(String userName, String token) {
        return userRestClient
                .get()
                .uri("/{userName}", userName)
                .header("Authorization", "Bearer " + token)
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

    public UserInfo isValid(String token) {
        return userRestClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("").queryParam("token", token).build())
                .retrieve()
                .body(UserInfo.class);
    }
}
