package pl.kamjer.ShoppingListRecipesServics.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
public class UserRequestDto {

    @NotEmpty
    private String userName;
    private String password;
}
