package pl.kamjer.ShoppingListRecipeService.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class StepDto {

    @NotNull
    private Integer stepNumber;
    @NotBlank @Size(max = 255)
    private String instruction;
}
