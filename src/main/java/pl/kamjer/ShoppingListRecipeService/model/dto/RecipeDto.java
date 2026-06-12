package pl.kamjer.ShoppingListRecipeService.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class RecipeDto {

    private Long recipeId;
    @NotBlank @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String description;
    @Valid
    private List<IngredientDto> ingredients;
    @Valid
    private List<StepDto> steps;
    @Valid
    private List<TagDto> tags;
    private String userName;
    @Size(max = 255)
    private String source;
    private Boolean published;
}
