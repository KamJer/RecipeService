package pl.kamjer.ShoppingListRecipeService.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class RecipeRequestDto {
    @NotEmpty
    List<String> products;
    @Min(0)
    Integer maxMissing;
}
