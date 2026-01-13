package pl.kamjer.ShoppingListRecipesServics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class RecipeDto {

    private Long recipeId;
    private String name;
    private String description;
    private List<IngredientDto> ingredients;
    private List<StepDto> steps;
    private List<TagDto> tags;
    private String source;
    private Boolean published;
}
