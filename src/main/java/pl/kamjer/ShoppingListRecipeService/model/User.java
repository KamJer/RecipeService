package pl.kamjer.ShoppingListRecipeService.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    private String userName;
    @JsonProperty("password")
    private String token;
    private LocalDateTime savedTime;
}
