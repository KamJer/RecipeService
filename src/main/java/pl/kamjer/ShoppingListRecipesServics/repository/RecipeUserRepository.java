package pl.kamjer.ShoppingListRecipesServics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.kamjer.ShoppingListRecipesServics.model.Recipe;
import pl.kamjer.ShoppingListRecipesServics.model.RecipeUser;
import pl.kamjer.ShoppingListRecipesServics.model.RecipeUserId;

import java.util.List;

@Repository
public interface RecipeUserRepository extends JpaRepository<RecipeUser, RecipeUserId> {
    @Query("""
                select ru from RecipeUser ru
                join fetch ru.recipe
                where ru.id.userName = :userName
            """)
    List<RecipeUser> findByRecipeUserIdUserName(String userName);
}
