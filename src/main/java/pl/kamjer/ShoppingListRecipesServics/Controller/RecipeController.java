package pl.kamjer.ShoppingListRecipesServics.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.ShoppingListRecipesServics.model.Recipe;
import pl.kamjer.ShoppingListRecipesServics.model.Tag;
import pl.kamjer.ShoppingListRecipesServics.model.dto.RecipeDto;
import pl.kamjer.ShoppingListRecipesServics.model.dto.RecipeRequestDto;
import pl.kamjer.ShoppingListRecipesServics.model.dto.TagDto;
import pl.kamjer.ShoppingListRecipesServics.services.RecipeService;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/recipe")
public class RecipeController {

    private RecipeService recipeService;
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
    public ResponseEntity<Boolean> deleteRecipe(@PathVariable Long recipeId) {
        recipeService.deleteRecipe(recipeId);
        return ResponseEntity.ok(true);
    }

    @GetMapping(path = "/id/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) throws IllegalAccessException {
        return ResponseEntity.ok(objectMapper.convertValue(recipeService.getRecipeById(id), RecipeDto.class));
    }

    @PostMapping(path = "/products")
    public ResponseEntity<Page<RecipeDto>> getRecipeByQuery(@RequestBody RecipeRequestDto recipeRequestDto, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByProducts(recipeRequestDto.getProducts(), recipeRequestDto.getMaxMissing(), pageable).map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)));
    }

    @GetMapping(path = "/name/{query}")
    public ResponseEntity<Page<RecipeDto>> getRecipeByQuery(@PathVariable String query, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByQuery(query, pageable).map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)));
    }

    @PostMapping(path = "/tag")
    public ResponseEntity<Page<RecipeDto>> getRecipeByTags(@RequestBody Set<TagDto> tags, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByTags(tags.stream().map(tagDto -> objectMapper.convertValue(tagDto, Tag.class)).collect(Collectors.toSet()), pageable).map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)));
    }

    @GetMapping(path = "/user")
    public ResponseEntity<Page<RecipeDto>> getRecipeForUser(Pageable pageable) throws IllegalAccessException {
        return ResponseEntity.ok(recipeService.getRecipeByUser(pageable).map(recipeUser -> objectMapper.convertValue(recipeUser, RecipeDto.class)));
    }

    @PostMapping(path = "/tag/required")
    public ResponseEntity<Page<RecipeDto>> getRecipeByTagsRequired(@RequestBody Set<TagDto> tags, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByTagsRequired(tags.stream().map(tagDto -> objectMapper.convertValue(tagDto, Tag.class)).collect(Collectors.toSet()), pageable).map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)));
    }

    @GetMapping()
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(Pageable pageable) {
        return ResponseEntity.ok(recipeService.getAllRecipes(pageable).map(recipe -> objectMapper.convertValue(recipe, RecipeDto.class)));
    }
}
