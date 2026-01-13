package pl.kamjer.ShoppingListRecipesServics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class IngredientDto {

    private String name;

    private Double quantity;

    private String unit;
}
