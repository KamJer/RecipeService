package pl.kamjer.ShoppingListRecipesServics.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeUserId implements Serializable {

    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    private String userName;
    @Column(name = "recipe_id")
    @EqualsAndHashCode.Include
    private Long recipeId;
}
