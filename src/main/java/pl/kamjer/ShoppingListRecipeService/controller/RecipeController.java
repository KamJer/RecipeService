package pl.kamjer.ShoppingListRecipeService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.Recipe;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.model.dto.IngredientDto;
import pl.kamjer.ShoppingListRecipeService.model.dto.RecipeDto;
import pl.kamjer.ShoppingListRecipeService.model.dto.StepDto;
import pl.kamjer.ShoppingListRecipeService.services.RecipeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/recipe")
public class RecipeController {

    private RecipeService recipeService;
    private ObjectMapper objectMapper;

    private RecipeDto toDto(Recipe recipe) {
        return RecipeDto.builder()
                .recipeId(recipe.getRecipeId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .ingredients(Optional.ofNullable(recipe.getIngredients()).orElse(List.of()).stream()
                        .map(i -> objectMapper.convertValue(i, IngredientDto.class))
                        .toList())
                .steps(Optional.ofNullable(recipe.getSteps()).orElse(List.of()).stream()
                        .map(s -> objectMapper.convertValue(s, StepDto.class))
                        .toList())
                .tags(Optional.ofNullable(recipe.getTags()).orElse(Set.of()).stream()
                        .map(Tag::getTag)
                        .toList())
                .userName(recipe.getUserName())
                .source(recipe.getSource())
                .published(recipe.getPublished())
                .build();
    }

    private Recipe toRecipe(RecipeDto recipeDto) {
        Recipe recipe = objectMapper.convertValue(recipeDto, Recipe.class);
        if (recipeDto.getTags() != null) {
            recipe.setTags(recipeDto.getTags().stream()
                    .map(t -> Tag.builder().tag(t).build())
                    .collect(Collectors.toSet()));
        }
        return recipe;
    }

    @PutMapping
    public ResponseEntity<RecipeDto> putRecipe(@Valid @RequestBody RecipeDto recipeDto) throws WrongRecipeElementException {
        log.info("PUT /recipe - creating recipe '{}'", recipeDto.getName());
        return ResponseEntity.ok(toDto(recipeService.insertRecipe(toRecipe(recipeDto))));
    }

    @PostMapping
    public ResponseEntity<Boolean> postRecipe(@Valid @RequestBody RecipeDto recipeDto) throws IllegalAccessException, WrongRecipeElementException {
        log.info("POST /recipe - updating recipe id={}", recipeDto.getRecipeId());
        recipeService.updateRecipe(toRecipe(recipeDto));
        return ResponseEntity.ok(true); 
    }

    @DeleteMapping(path = "/{recipeId}")
    public ResponseEntity<Boolean> deleteRecipe(@PathVariable Long recipeId) throws IllegalAccessException {
        log.info("DELETE /recipe/{} - deleting recipe", recipeId);
        recipeService.deleteRecipe(recipeId);
        return ResponseEntity.ok(true);
    }

    @GetMapping(path = "/id/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(toDto(recipeService.getRecipeById(id)));
    }

    @GetMapping(path = "/name/{query}")
    public ResponseEntity<Page<RecipeDto>> getRecipeByQuery(@PathVariable String query, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByQuery(query, pageable).map(this::toDto));
    }

    @GetMapping(path = "/exact/{name}")
    public ResponseEntity<RecipeDto> getRecipeByExactName(@PathVariable String name) {
        return recipeService.getRecipeByExactName(name)
                .map(recipe -> ResponseEntity.ok(toDto(recipe)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/ingredients")
    public ResponseEntity<Page<RecipeDto>> getRecipeByIngredients(@RequestBody List<String> ingredients, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipesByIngredientNames(ingredients, pageable).map(this::toDto));
    }

    @PostMapping(path = "/tag")
    public ResponseEntity<Page<RecipeDto>> getRecipeByTags(@RequestBody Set<String> tags, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByTags(tags, pageable).map(this::toDto));
    }

    @GetMapping(path = "/user")
    public ResponseEntity<Page<RecipeDto>> getRecipeForUser(Pageable pageable) throws IllegalAccessException {
        return ResponseEntity.ok(recipeService.getRecipeByUser(pageable).map(this::toDto));
    }

    @PostMapping(path = "/tag/required")
    public ResponseEntity<Page<RecipeDto>> getRecipeByTagsRequired(@RequestBody Set<String> tags, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByTagsRequired(tags, pageable).map(this::toDto));
    }

    @GetMapping()
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(Pageable pageable) {
        return ResponseEntity.ok(recipeService.getAllRecipes(pageable).map(this::toDto));
    }

}
