package pl.kamjer.ShoppingListRecipeService.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class IngredientDto {

    @NotBlank @Size(max = 255)
    private String name;
    private Double quantity;
    @Size(max = 255)
    private String unit;
}
