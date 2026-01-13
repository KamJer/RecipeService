package pl.kamjer.ShoppingListRecipesServics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class RecipeRequestDto {
    List<String> products;
    Integer maxMissing;
}
