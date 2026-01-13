package pl.kamjer.ShoppingListRecipesServics.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.ShoppingListRecipesServics.model.Recipe;
import pl.kamjer.ShoppingListRecipesServics.model.Tag;
import pl.kamjer.ShoppingListRecipesServics.model.dto.RecipeDto;
import pl.kamjer.ShoppingListRecipesServics.model.dto.RecipeRequestDto;
import pl.kamjer.ShoppingListRecipesServics.model.dto.TagDto;
import pl.kamjer.ShoppingListRecipesServics.services.RecipeService;
import pl.kamjer.ShoppingListRecipesServics.services.RecipeUserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/recipe")
public class RecipeController {

    private RecipeService recipeService;
    private RecipeUserService recipeUserService;
    private ObjectMapper objectMapper;

    @PutMapping
    public ResponseEntity<RecipeDto> putRecipe(@RequestBody RecipeDto recipeDto) throws IllegalAccessException {
        return ResponseEntity.ok(objectMapper.convertValue(recipeService.insertRecipe(objectMapper.convertValue(recipeDto, Recipe.class)), RecipeDto.class));
    }

    @PostMapping
    public ResponseEntity<Boolean> postRecipe(@RequestBody RecipeDto recipeDto) throws IllegalAccessException {
        recipeService.updateRecipe(objectMapper.convertValue(recipeDto, Recipe.class));
        return ResponseEntity.ok(true);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteRecipe(@RequestBody Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.ok(true);
    }

    @PutMapping(path = "/for_user/{recipeId}")
    public ResponseEntity<Boolean> putRecipeForUser(@PathVariable Long recipeId) throws IllegalAccessException {
        recipeUserService.insertRecipeForUser(recipeId);
        return ResponseEntity.ok(true);
    }
    @DeleteMapping(path = "/for_user/{recipeId}")
    public ResponseEntity<Boolean> deleteRecipeForUser(@PathVariable Long recipeId) {
        recipeUserService.deleteRecipeForUser(recipeId);
        return ResponseEntity.ok(true);
    }

    @GetMapping(path = "/id/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) throws IllegalAccessException {
        return ResponseEntity.ok(objectMapper.convertValue(recipeService.getRecipeById(id), RecipeDto.class));
    }

    @GetMapping(path = "/products")
    public ResponseEntity<List<RecipeDto>> getRecipeByProducts(@RequestBody RecipeRequestDto recipeRequestDto) {
        return ResponseEntity.ok(recipeService.getRecipeByProducts(recipeRequestDto.getProducts(), recipeRequestDto.getMaxMissing()).stream().map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)).toList());
    }

    @GetMapping(path = "/name/{query}")
    public ResponseEntity<List<RecipeDto>> getRecipeByProducts(@PathVariable String query) {
        return ResponseEntity.ok(recipeService.getRecipeByQuery(query).stream().map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)).toList());
    }

    @GetMapping(path = "/tag")
    public ResponseEntity<List<RecipeDto>> getRecipeByTags(@RequestBody Set<TagDto> tags) {
        return ResponseEntity.ok(recipeService.getRecipeByTags(tags.stream().map(tagDto -> objectMapper.convertValue(tagDto, Tag.class)).collect(Collectors.toSet())).stream().map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)).toList());
    }

    @GetMapping(path = "/user/{userName}")
    public ResponseEntity<List<RecipeDto>> getRecipeForUser(@PathVariable String userName) {
        return ResponseEntity.ok(recipeUserService.getRecipeByUser(userName).stream().map(recipeUser -> objectMapper.convertValue(recipeUser.getRecipe(), RecipeDto.class)).toList());
    }

    @GetMapping(path = "/tag/required")
    public ResponseEntity<List<RecipeDto>> getRecipeByTagsRequired(@RequestBody Set<TagDto> tags) {
        return ResponseEntity.ok(recipeService.getRecipeByTagsRequired(tags.stream().map(tagDto -> objectMapper.convertValue(tagDto, Tag.class)).collect(Collectors.toSet())).stream().map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)).toList());
    }
}
