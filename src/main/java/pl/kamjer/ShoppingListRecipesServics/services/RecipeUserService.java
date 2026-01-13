package pl.kamjer.ShoppingListRecipesServics.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipesServics.model.Recipe;
import pl.kamjer.ShoppingListRecipesServics.model.RecipeUser;
import pl.kamjer.ShoppingListRecipesServics.model.RecipeUserId;
import pl.kamjer.ShoppingListRecipesServics.model.User;
import pl.kamjer.ShoppingListRecipesServics.repository.RecipeRepository;
import pl.kamjer.ShoppingListRecipesServics.repository.RecipeUserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RecipeUserService {
    private RecipeUserRepository recipeUserRepository;
    private RecipeRepository recipeRepository;
    private UserService userService;

    @Transactional
    public List<RecipeUser> getRecipeByUser(String userName) {
        return recipeUserRepository.findByRecipeUserIdUserName(userName);
    }

    @Transactional
    public void insertRecipeForUser(Long recipeId) throws IllegalAccessException {
        Optional<User> userOp = userService.getUserFromAuth();
        if (userOp.isPresent()) {
            User user = userOp.get();
            Recipe recipeToBind = recipeRepository.findById(recipeId).orElseThrow(NoSuchElementException::new);
            RecipeUserId recipeUserId = new RecipeUserId(user.getUserName(), recipeId);
            RecipeUser recipeUser = RecipeUser.builder()
                    .recipeUserId(recipeUserId)
                    .recipe(recipeToBind)
                    .build();
            recipeUserRepository.save(recipeUser);
        } else {
            throw new IllegalAccessException("Only a valid user can insert a recipe");
        }
    }

    @Transactional
    public void deleteRecipeForUser(Long recipeId) {
        Optional<User> userOp = userService.getUserFromAuth();
        if (userOp.isPresent()) {
            User user = userOp.get();
            RecipeUserId recipeUserId = new RecipeUserId(user.getUserName(), recipeId);
            recipeUserRepository.deleteById(recipeUserId);

        }
    }
}
