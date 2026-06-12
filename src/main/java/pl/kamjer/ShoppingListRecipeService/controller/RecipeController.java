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
import pl.kamjer.ShoppingListRecipeService.model.dto.RecipeDto;
import pl.kamjer.ShoppingListRecipeService.model.dto.RecipeRequestDto;
import pl.kamjer.ShoppingListRecipeService.model.dto.TagDto;
import pl.kamjer.ShoppingListRecipeService.services.RecipeService;

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
        return objectMapper.convertValue(recipe, RecipeDto.class);
    }

    @PutMapping
    public ResponseEntity<RecipeDto> putRecipe(@Valid @RequestBody RecipeDto recipeDto) throws WrongRecipeElementException {
        log.info("PUT /recipe - creating recipe '{}'", recipeDto.getName());
        return ResponseEntity.ok(toDto(recipeService.insertRecipe(objectMapper.convertValue(recipeDto, Recipe.class))));
    }

    @PostMapping
    public ResponseEntity<Boolean> postRecipe(@Valid @RequestBody RecipeDto recipeDto) throws IllegalAccessException, WrongRecipeElementException {
        log.info("POST /recipe - updating recipe id={}", recipeDto.getRecipeId());
        recipeService.updateRecipe(objectMapper.convertValue(recipeDto, Recipe.class));
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

    @PostMapping(path = "/products")
    public ResponseEntity<Page<RecipeDto>> getRecipeByProducts(@Valid @RequestBody RecipeRequestDto recipeRequestDto, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByProducts(recipeRequestDto.getProducts(), recipeRequestDto.getMaxMissing(), pageable).map(this::toDto));
    }

    @GetMapping(path = "/name/{query}")
    public ResponseEntity<Page<RecipeDto>> getRecipeByQuery(@PathVariable String query, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByQuery(query, pageable).map(this::toDto));
    }

    @PostMapping(path = "/products/required")
    public ResponseEntity<Page<RecipeDto>> getRecipeByProductsRequired(@Valid @RequestBody RecipeRequestDto recipeRequestDto, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByProductsRequired(recipeRequestDto.getProducts(), pageable).map(this::toDto));
    }

    @PostMapping(path = "/tag")
    public ResponseEntity<Page<RecipeDto>> getRecipeByTags(@Valid @RequestBody Set<TagDto> tags, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByTags(tags.stream().map(tagDto -> objectMapper.convertValue(tagDto, Tag.class)).collect(Collectors.toSet()), pageable).map(this::toDto));
    }

    @GetMapping(path = "/user")
    public ResponseEntity<Page<RecipeDto>> getRecipeForUser(Pageable pageable) throws IllegalAccessException {
        return ResponseEntity.ok(recipeService.getRecipeByUser(pageable).map(this::toDto));
    }

    @PostMapping(path = "/tag/required")
    public ResponseEntity<Page<RecipeDto>> getRecipeByTagsRequired(@Valid @RequestBody Set<TagDto> tags, Pageable pageable) {
        return ResponseEntity.ok(recipeService.getRecipeByTagsRequired(tags.stream().map(tagDto -> objectMapper.convertValue(tagDto, Tag.class)).collect(Collectors.toSet()), pageable).map(this::toDto));
    }

    @GetMapping()
    public ResponseEntity<Page<RecipeDto>> getAllRecipes(Pageable pageable) {
        return ResponseEntity.ok(recipeService.getAllRecipes(pageable).map(this::toDto));
    }

}
