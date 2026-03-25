package pl.kamjer.ShoppingListRecipesServics.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserInfo {

    private String userName;
    private String role;
}
