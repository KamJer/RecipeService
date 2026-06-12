package pl.kamjer.ShoppingListRecipeService.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class TagDto {
    @NotBlank @Size(max = 255)
    private String tag;
}
