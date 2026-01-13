    package pl.kamjer.ShoppingListRecipesServics.model;

    import jakarta.persistence.*;
    import lombok.*;

    @Entity
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class RecipeUser {
        @EmbeddedId
        private RecipeUserId recipeUserId;

        @MapsId("recipeId")
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "recipe_id", nullable = false)
        private Recipe recipe;
    }
